package proover.verifier

import java.io.File
import scala.collection.mutable

import leo.modules.input.TPTPParser.TPTPParseException

import proover.syntax.*
import proover.solver.{Consequence, VampireBridge}
import proover.parser.{TptpReader, TptpMappingException}

/** An SZS status value (https://tptp.org/UserDocs/SZSOntology/), restricted to
  * those relevant to proof verification.
  *
  *   - [[Verified]] (VER): the proof has been verified correct.
  *   - [[FailedVerified]] (FVE): the proof failed verification (it is invalid).
  *   - [[NotVerified]] (NVE): the proof could not be verified either way.
  *   - [[Error]] (ERR): the checker itself stopped with an error.
  */
enum SzsStatus(val oneWord: String, val mnemonic: String):
  case Verified       extends SzsStatus("Verified", "VER")
  case FailedVerified extends SzsStatus("FailedVerified", "FVE")
  case NotVerified    extends SzsStatus("NotVerified", "NVE")
  case Error          extends SzsStatus("Error", "ERR")

/** How serious a [[Finding]] is, and thus how it influences the verdict.
  *
  *   - [[Error]]: a definite violation — drives the result to `FailedVerified`.
  *   - [[Warning]]: a non-fatal concern — recorded but does not change the verdict.
  *   - [[Unchecked]]: an obligation that could not be discharged — drives the
  *     result to `NotVerified` (unless some [[Error]] takes precedence).
  */
enum Severity:
  case Error, Warning, Unchecked

/** A single finding produced by a check, optionally attributed to a step. */
case class Finding(severity: Severity, message: String, step: Option[String] = None):
  override def toString: String = step.fold(message)(s => s"$s: $message")

/** The outcome of verifying a proof: an SZS [[status]], a human-readable
  * [[description]], and all [[findings]] (errors, warnings and undischarged
  * obligations) gathered along the way.
  */
case class VerificationResult(status: SzsStatus, description: String, findings: List[Finding] = Nil):
  def errors: List[Finding]    = findings.filter(_.severity == Severity.Error)
  def warnings: List[Finding]  = findings.filter(_.severity == Severity.Warning)
  def unchecked: List[Finding] = findings.filter(_.severity == Severity.Unchecked)

  /** The competition output line, e.g. `% SZS status FailedVerified for foo : ...`. */
  def szsLine(problemName: String): String =
    val suffix: String = if description.isEmpty then "" else s" : $description"
    s"% SZS status ${status.oneWord} for $problemName$suffix"

/** Checks ProoVer proofs and reports an SZS verdict.
  *
  * Verification is a single procedural descent ([[Run.run]]) that reads top to
  * bottom as the verification story. Each source of error/obligation is its own
  * small `check…` function returning its [[Finding]]s; `run` accumulates them
  * and, at phase boundaries, returns early with the partial result once a phase
  * has produced an error (no point checking inference rules on a broken DAG).
  *
  * Checks are added one at a time as they are implemented. So far:
  * [[Run.checkAndBuildStepList]] (name freshness + exactly one conjecture),
  * [[Run.checkDAGBuildMap]]
  * (unresolved parents + acyclicity), [[Run.checkAxiom]] / [[Run.checkConjecture]]
  * (leaves match the problem by role and up to alpha-equivalence),
  * [[Run.checkNegatedConjecture]] (status `cth`, parent is the conjecture, and
  * the formula negates it), [[Run.checkPlainInferenceStep]] (each free inference
  * is a logical consequence of its parents, via the prover), and
  * [[Run.checkProvesConjectureOrFalse]] (some step reaches the goal — `$false`
  * or the conjecture).
  */
object Verifier:

  def verify(proof: ProofFile, problem: Option[ProblemFile] = None): VerificationResult =
    try Run(proof, problem).run
    catch case e: Throwable => VerificationResult(SzsStatus.Error, s"internal error: ${e.getMessage}")

  /** Read, parse and verify proof (and optional problem) files. Any parse or
    * mapping failure is reported as an SZS `Error` with a descriptive message
    * rather than thrown, so callers always get a verdict. */
  def verifyFile(proofFile: File, problemFile: Option[File] = None): VerificationResult =
    verifyParsed(TptpReader.readProof(proofFile), problemFile.map(f => () => TptpReader.readProblem(f)))

  /** Like [[verifyFile]] but from in-memory TPTP source strings. */
  def verifyString(proofContent: String, problemContent: Option[String] = None): VerificationResult =
    verifyParsed(
      TptpReader.readProofString(proofContent),
      problemContent.map(c => () => TptpReader.readProblemString(c))
    )

  /** Parse the proof, then the optional problem, then verify — short-circuiting to
    * an SZS `Error` on the first parse failure. */
  private def verifyParsed(parseProof: => ProofFile, parseProblem: Option[() => ProblemFile]): VerificationResult =
    parse("proof", parseProof) match
      case Left(err) => err
      case Right(proof) =>
        parseProblem match
          case None => verify(proof, None)
          case Some(parseIt) =>
            parse("problem", parseIt()) match
              case Left(err)      => err
              case Right(problem) => verify(proof, Some(problem))

  /** Run one parse, turning a failure into a descriptive SZS `Error` result. The
    * label (`"proof"` / `"problem"`) tells the caller which file was at fault. */
  private def parse[A](what: String, thunk: => A): Either[VerificationResult, A] =
    try Right(thunk)
    catch
      case e: TptpMappingException => Left(parseError(what, e.getMessage))
      case e: TPTPParseException   => Left(parseError(what, e.getMessage))
      case _: StackOverflowError   => Left(parseError(what, "formula is nested too deeply to parse"))
      case e: Throwable            => Left(VerificationResult(SzsStatus.Error, s"could not read $what file: ${e.getMessage}"))

  private def parseError(what: String, message: String): VerificationResult =
    VerificationResult(SzsStatus.Error, s"$what parse error: $message")

  private final class Run(proof: ProofFile, problem: Option[ProblemFile]):
    private val steps: List[Step]                  = proof.steps
    private val findings: mutable.ListBuffer[Finding] = mutable.ListBuffer.empty[Finding]

    /** The problem's steps by name (all leaves are assumed to reference the same
      * problem file); carries the role so leaf checks can require it. */
    private val problemSteps: Map[String, Step] =
      problem.fold(Map.empty[String, Step])(_.steps.map(s => s.name -> s).toMap)

    /** The verification narrative. Checks append to [[findings]] directly; `run`
      * only decides phase boundaries and assembles the result.
      */
    def run: VerificationResult =
      // Signature sanity (warnings only) — always reported.
      checkSymbolArityConsistency()

      // Phase 1 — structure / well-formedness of the derivation.
      val (freshSteps, maybeConj): (List[Step], Option[Step.Conjecture]) = checkAndBuildStepList()
      if hasError then return result
      val conj: Step.Conjecture = maybeConj.get // present: 0 / >1 conjectures recorded an error above
      val byName: Map[String, Step] = checkDAGBuildMap(freshSteps)
      if hasError then return result

      // Phase 2 — per-step verification (leaves against the problem, plain
      // inferences against their parents). The conjecture was set aside above.
      checkConjecture(conj)
      for step <- byName.values do
        checkAxiom(step)
        checkNegatedConjecture(step, conj)
        checkPlainInferenceStep(step, byName)

      // Phase 3 — the proof must reach its goal.
      checkProvesConjectureOrFalse(conj, byName.values)
      result

    /** Record a definite violation (drives the result to `FailedVerified`). */
    private def error(message: String, step: String): Unit =
      findings += Finding(Severity.Error, message, Some(step))

    private def error(message: String): Unit =
      findings += Finding(Severity.Error, message)

    /** Record an obligation that could not be discharged (drives the result to
      * `NotVerified`, unless an error takes precedence). */
    private def unverified(message: String, step: String): Unit =
      findings += Finding(Severity.Unchecked, message, Some(step))

    /** Record a non-fatal concern (does not affect the verdict). */
    private def warning(message: String): Unit =
      findings += Finding(Severity.Warning, message)

    // -- Checks ---------------------------------------------------------------

    /** Warn about any symbol used with more than one arity (e.g. `p` and `p(a)`),
      * over the union of the proof's and problem's signatures (a clash may span
      * the two files). Signatures are accumulated at parse time; the warning names
      * the annotated formula where each clashing arity was first seen.
      * Warning-level: distinct arities are distinct FOL symbols, so this is
      * suspicious but not unsound.
      */
    private def checkSymbolArityConsistency(): Unit =
      val problemSig: Signature = problem.map(_.signature).getOrElse(Map.empty)
      val combined: Signature =
        (proof.signature.keySet ++ problemSig.keySet).iterator
          .map(name => name -> (problemSig.getOrElse(name, Map.empty) ++ proof.signature.getOrElse(name, Map.empty)))
          .toMap
      for (name, byArity) <- combined.toList.sortBy(_._1) if byArity.sizeIs > 1 do
        val where: String =
          byArity.toList.sortBy(_._1).map((arity, formulaName) => s"arity $arity in '$formulaName'").mkString(", ")
        warning(s"symbol '$name' is used with inconsistent arities: $where")

    /** Check that every step name is fresh (a duplicate name is an error) and
      * that there is exactly one conjecture. Returns the uniquely-named
      * non-conjecture steps together with the conjecture (`None`, plus a recorded
      * error, if there is not exactly one). The name → step map itself is
      * produced later, in topological order, by [[checkDAGBuildMap]].
      */
    private def checkAndBuildStepList(): (List[Step], Option[Step.Conjecture]) =
      val byName: mutable.LinkedHashMap[String, Step] = mutable.LinkedHashMap.empty[String, Step]
      for s <- steps do
        if byName.contains(s.name) then error("step name is not fresh (already used)", s.name)
        else byName(s.name) = s
      val unique: List[Step] = byName.values.toList

      val conjectures: List[Step.Conjecture] = unique.collect { case c: Step.Conjecture => c }
      val conj: Option[Step.Conjecture] = conjectures match
        case List(only) => Some(only)
        case Nil =>
          error("proof has no conjecture step")
          None
        case several =>
          several.foreach(c => error("proof has more than one conjecture step", c.name))
          None

      (unique.filterNot(_.isInstanceOf[Step.Conjecture]), conj)

    /** Check that every parent reference resolves to a known step and that the
      * derivation is acyclic, returning the name → step map in topological order
      * (parents before children) for later phases. Acyclicity falls out of the
      * topological sort (Kahn's algorithm): a step that cannot be emitted lies on
      * a cycle. Cycle members, if any, are appended so the map stays complete.
      */
    private def checkDAGBuildMap(steps: List[Step]): Map[String, Step] =
      val byName: Map[String, Step] = steps.map(s => s.name -> s).toMap

      // Resolve each step's premises once, reporting any unresolved reference.
      // (A negated_conjecture's parent is the conjecture, which has no premise
      // edge here — it is handled by checkNegatedConjecture.)
      val parents: Map[String, List[String]] = steps.map { s =>
        val (known, unknown): (List[String], List[String]) = s.premises.partition(byName.contains)
        unknown.foreach(p => error(s"unresolved parent reference '$p'", s.name))
        s.name -> known
      }.toMap

      // Topological sort (Kahn's algorithm) over the resolvable parent edges.
      val inDegree: mutable.Map[String, Int] = mutable.Map.from(parents.view.mapValues(_.size))
      val children: mutable.Map[String, mutable.ListBuffer[String]] =
        steps.map(s => s.name -> mutable.ListBuffer.empty[String]).to(mutable.Map)
      for (child, ps) <- parents; p <- ps do children(p) += child

      val queue: mutable.Queue[String]      = mutable.Queue.from(steps.iterator.map(_.name).filter(inDegree(_) == 0))
      val ordered: mutable.ListBuffer[Step] = mutable.ListBuffer.empty[Step]
      while queue.nonEmpty do
        val n: String = queue.dequeue()
        ordered += byName(n)
        for c <- children(n) do
          inDegree(c) -= 1
          if inDegree(c) == 0 then queue.enqueue(c)

      if ordered.sizeIs < byName.size then
        error("derivation contains a cycle (cannot be topologically ordered)")

      // Map in topological order; any cycle members keep their input order at the end.
      val emitted: Set[String]     = ordered.iterator.map(_.name).toSet
      val cycleMembers: List[Step] = steps.filterNot(s => emitted(s.name))
      scala.collection.immutable.SeqMap.from((ordered.toList ++ cycleMembers).map(s => s.name -> s))

    /** Verify a leaf (axiom / conjecture) against the problem: its formula must
      * be alpha-equivalent to the problem formula named by its `file(...)`
      * reference. Non-leaf steps are ignored here.
      */
    /** Verify an axiom leaf: the problem formula it cites must exist, be an
      * axiom (not the conjecture), and be alpha-equivalent. Non-axiom steps are
      * ignored.
      */
    private def checkAxiom(step: Step): Unit = step match
      case a: Step.Axiom => checkLeaf("axiom", a.name, a.formula, a.source, requireConjecture = false)
      case _             => ()

    /** Verify the conjecture leaf: the problem formula it cites must exist, be
      * the conjecture, and be alpha-equivalent. */
    private def checkConjecture(conj: Step.Conjecture): Unit =
      checkLeaf("conjecture", conj.name, conj.formula, conj.source, requireConjecture = true)

    /** Shared leaf-against-problem check: the cited formula exists, has the
      * expected role (axiom vs. conjecture), and is alpha-equivalent.
      */
    private def checkLeaf(role: String, name: String, formula: Formula, source: Option[FileSource], requireConjecture: Boolean): Unit =
      (problem, source) match
        case (None, _) =>
          unverified(s"$role origin not checked (no problem file provided)", name)
        case (Some(_), None) =>
          error(s"$role has no problem-file reference", name)
        case (Some(_), Some(FileSource(_, formulaName))) =>
          problemSteps.get(formulaName) match
            case None =>
              error(s"$role refers to '$formulaName', which is not in the problem file", name)
            case Some(target) =>
              val targetIsConjecture: Boolean = target.isInstanceOf[Step.Conjecture]
              if targetIsConjecture != requireConjecture then
                val targetRole: String = if targetIsConjecture then "the conjecture" else "an axiom"
                error(s"$role refers to '$formulaName', which is $targetRole in the problem", name)
              else if !alphaEquivalent(formula, target.formula) then
                error(s"$role does not match problem formula '$formulaName' (up to variable renaming)", name)

    /** Verify a negated_conjecture step: status `cth`, its single parent is the
      * conjecture, and its formula is the negation of the conjecture
      * (`N ⟺ ¬C` valid, decided by the prover). Other step kinds are ignored.
      */
    private def checkNegatedConjecture(step: Step, conj: Step.Conjecture): Unit = step match
      case n: Step.NegatedConjecture =>
        if n.status != Status.Cth then
          error("negated_conjecture must have status cth", n.name)
        n.parents match
          case List(p) if p == conj.name =>
            VampireBridge.isConsequence(Nil, Formula.Iff(n.formula, Formula.Not(conj.formula))) match
              case Consequence.Yes     => ()
              case Consequence.No      => error("formula is not the negation of the conjecture", n.name)
              case Consequence.Unknown => unverified("could not verify the negation of the conjecture", n.name)
          case List(p) =>
            error(s"negated_conjecture's parent '$p' is not the conjecture '${conj.name}'", n.name)
          case ps =>
            error(s"negated_conjecture must have exactly one parent (the conjecture), found ${ps.size}", n.name)
      case _ => ()

    /** Verify that a plain (free) inference step's formula is a first-order
      * logical consequence of its parents, using the external prover. Other step
      * kinds are not verified here. Parents are guaranteed to resolve, as this
      * phase only runs once [[checkDAGBuildMap]] has succeeded.
      */
    private def checkPlainInferenceStep(step: Step, byName: Map[String, Step]): Unit = step match
      case pi: Step.PlainInference =>
        val premises: List[Formula] = pi.parents.map(name => byName(name).formula)
        VampireBridge.isConsequence(premises, pi.formula) match
          case Consequence.Yes     => ()
          case Consequence.No      => error("formula is not a logical consequence of its parents", pi.name)
          case Consequence.Unknown => unverified("could not verify the inference (no prover answer)", pi.name)
      case _ => ()

    /** Verify that the proof reaches its goal. Deriving the conjecture directly is
      * always fully correct. Deriving `$false` is fully correct as a refutation
      * when a negated_conjecture is present; without one it is still correct but
      * warned — `$false` then only shows the axioms unsatisfiable, so the
      * conjecture holds merely vacuously. Reaching neither is an error.
      */
    private def checkProvesConjectureOrFalse(conj: Step.Conjecture, steps: Iterable[Step]): Unit =
      val provesConjecture: Boolean     = steps.exists(s => alphaEquivalent(s.formula, conj.formula))
      val provesFalse: Boolean          = steps.exists(_.formula == Formula.False)
      val hasNegatedConjecture: Boolean = steps.exists(_.isInstanceOf[Step.NegatedConjecture])
      if provesConjecture then ()
      else if provesFalse then
        if !hasNegatedConjecture then
          warning("reaches $false without a negated_conjecture step; the conjecture holds only vacuously (the axioms are unsatisfiable)")
      else
        error("no step derives $false or proves the conjecture")

    // -- Result assembly ------------------------------------------------------

    private def hasError: Boolean = findings.exists(_.severity == Severity.Error)

    private def result: VerificationResult =
      val all: List[Finding] = findings.toList
      val status: SzsStatus =
        if all.exists(_.severity == Severity.Error) then SzsStatus.FailedVerified
        else if all.exists(_.severity == Severity.Unchecked) then SzsStatus.NotVerified
        else SzsStatus.Verified
      VerificationResult(status, describe(status, all), all)

    private def describe(status: SzsStatus, all: List[Finding]): String = status match
      case SzsStatus.FailedVerified =>
        all.filter(_.severity == Severity.Error).map(_.toString).mkString("; ")
      case SzsStatus.NotVerified =>
        val obligations: List[Finding] = all.filter(_.severity == Severity.Unchecked)
        s"${obligations.size} obligation(s) undischarged: " + obligations.map(_.toString).mkString("; ")
      case SzsStatus.Verified =>
        s"${steps.size} step(s) verified"
      case SzsStatus.Error =>
        ""

  /** Formula equality up to consistent renaming of bound variables, decided by
    * canonicalising both formulas to a de Bruijn form and comparing structurally.
    * Free variables keep their names (alpha-equivalence does not rename them).
    */
  private def alphaEquivalent(f1: Formula, f2: Formula): Boolean =
    deBruijn(f1) == deBruijn(f2)

  /** Canonical de Bruijn form: every bound variable is rewritten to `#<depth>`
    * (the depth of its binder from the root) and every binder name is erased to
    * `_`; free variables, predicate and function symbols keep their names. Two
    * formulas are alpha-equivalent iff their de Bruijn forms are equal.
    */
  private def deBruijn(formula: Formula): Formula =
    def canonTerm(t: Term, env: Map[String, Int]): Term = t match
      case Term.Var(n)       => Term.Var(env.get(n).fold(n)(depth => s"#$depth"))
      case Term.App(n, args) => Term.App(n, args.map(canonTerm(_, env)))

    def canon(f: Formula, env: Map[String, Int], depth: Int): Formula = f match
      case Formula.Pred(n, args) => Formula.Pred(n, args.map(canonTerm(_, env)))
      case Formula.Eq(l, r)      => Formula.Eq(canonTerm(l, env), canonTerm(r, env))
      case Formula.True          => Formula.True
      case Formula.False         => Formula.False
      case Formula.Not(g)        => Formula.Not(canon(g, env, depth))
      case Formula.And(a, b)     => Formula.And(canon(a, env, depth), canon(b, env, depth))
      case Formula.Or(a, b)      => Formula.Or(canon(a, env, depth), canon(b, env, depth))
      case Formula.Implies(a, b) => Formula.Implies(canon(a, env, depth), canon(b, env, depth))
      case Formula.Iff(a, b)     => Formula.Iff(canon(a, env, depth), canon(b, env, depth))
      case Formula.Forall(v, b)  => Formula.Forall("_", canon(b, env + (v -> depth), depth + 1))
      case Formula.Exists(v, b)  => Formula.Exists("_", canon(b, env + (v -> depth), depth + 1))

    canon(formula, Map.empty, 0)
