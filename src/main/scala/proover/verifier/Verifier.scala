package proover.verifier

import java.io.File
import scala.collection.mutable
import scala.collection.immutable.SeqMap

import leo.modules.input.TPTPParser.TPTPParseException

import proover.syntax.*
import proover.solver.{Consequence, VampireBridge}
import proover.parser.{TptpReader, TptpMappingException}

/** The SZS status the checker reports (ProoVer ontology).
  *
  *   - [[VerifiedGood]]: the proof is valid.
  *   - [[VerifiedBad]]: the proof is invalid (a definite violation was found).
  *   - [[Timeout]]: the prover ran out of time on a step that had to be checked.
  *   - [[Unknown]]: the checker could not determine a verdict (e.g. the input did
  *     not parse, or an internal error).
  */
enum SzsStatus(val oneWord: String):
  case VerifiedGood extends SzsStatus("VerifiedGood")
  case VerifiedBad  extends SzsStatus("VerifiedBad")
  case Timeout      extends SzsStatus("Timeout")
  case Unknown      extends SzsStatus("Unknown")

/** How serious a [[Finding]] is, and thus how it influences the verdict.
  *
  *   - [[Error]]: a definite violation — drives the result to `VerifiedBad`.
  *   - [[Warning]]: a non-fatal concern — recorded but does not change the verdict.
  *   - [[Timeout]]: the prover did not answer in time on a step that had to be
  *     checked — drives the result to `Timeout` (unless some [[Error]] takes
  *     precedence).
  */
enum Severity:
  case Error, Warning, Timeout

/** A single finding produced by a check, optionally attributed to a step. */
case class Finding(severity: Severity, message: String, step: Option[String] = None):
  override def toString: String = step.fold(message)(s => s"$s: $message")

/** The outcome of verifying a proof: an SZS [[status]], a human-readable
  * [[description]], and all [[findings]] (errors, warnings and timeouts) gathered
  * along the way.
  */
case class VerificationResult(status: SzsStatus, description: String, findings: List[Finding] = Nil):
  def errors: List[Finding]   = findings.filter(_.severity == Severity.Error)
  def warnings: List[Finding]  = findings.filter(_.severity == Severity.Warning)
  def timeouts: List[Finding]  = findings.filter(_.severity == Severity.Timeout)

  /** The competition output line, e.g. `% SZS status VerifiedBad for foo : ...`. */
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

  /** Time budget (ms) for the prover on a single plain inference. ProoVer plain
    * steps are guaranteed easy, so exceeding this means the step is too complex to
    * be a valid plain inference (reported as an error). Defaults to 3000 ms;
    * override with the `PROOVER_PLAIN_TIMEOUT_MS` environment variable (e.g. to
    * tune for a slower or faster machine). */
  private val PlainStepTimeoutMs: Int =
    sys.env.get("PROOVER_PLAIN_TIMEOUT_MS").flatMap(_.toIntOption).filter(_ > 0).getOrElse(3000)

  /** Verify a proof.
    *
    * @param strictNegatedConjecture how the `negated_conjecture` is checked: in
    *   strict mode (the default) its formula must be *exactly* `¬conjecture` up to
    *   alpha-equivalence; in non-strict mode the prover decides the equivalence
    *   `negated_conjecture ⟺ ¬conjecture` instead (allowing a normalized negation,
    *   e.g. an NNF push-in).
    */
  def verify(proof: ProofFile, problem: Option[ProblemFile] = None, strictNegatedConjecture: Boolean = true): VerificationResult =
    try Run(proof, problem, strictNegatedConjecture).run
    catch case e: Throwable => VerificationResult(SzsStatus.Unknown, s"internal error: ${e.getMessage}")

  /** Read, parse and verify proof (and optional problem) files. Any parse or
    * mapping failure is reported as `VerifiedBad` (the input is not a well-formed
    * proof) with a descriptive message rather than thrown, so callers always get a
    * verdict. */
  def verifyFile(proofFile: File, problemFile: Option[File] = None, strictNegatedConjecture: Boolean = true): VerificationResult =
    verifyParsed(TptpReader.readProof(proofFile), problemFile.map(f => () => TptpReader.readProblem(f)), strictNegatedConjecture)

  /** Like [[verifyFile]] but from in-memory TPTP source strings. */
  def verifyString(proofContent: String, problemContent: Option[String] = None, strictNegatedConjecture: Boolean = true): VerificationResult =
    verifyParsed(
      TptpReader.readProofString(proofContent),
      problemContent.map(c => () => TptpReader.readProblemString(c)),
      strictNegatedConjecture
    )

  /** Parse the proof, then the optional problem, then verify — short-circuiting to
    * a `VerifiedBad` verdict on the first parse failure. */
  private def verifyParsed(parseProof: => ProofFile, parseProblem: Option[() => ProblemFile], strictNegatedConjecture: Boolean): VerificationResult =
    parse("proof", parseProof) match
      case Left(err) => err
      case Right(proof) =>
        parseProblem match
          case None => verify(proof, None, strictNegatedConjecture)
          case Some(parseIt) =>
            parse("problem", parseIt()) match
              case Left(err)      => err
              case Right(problem) => verify(proof, Some(problem), strictNegatedConjecture)

  /** Run one parse, turning a failure into a descriptive verdict. The label
    * (`"proof"` / `"problem"`) tells the caller which file was at fault. A
    * parse/mapping failure means the input is not a well-formed proof, so it is
    * `VerifiedBad`; a failure to *read* the file at all (e.g. missing file) is an
    * environment problem, so it is `Unknown`. */
  private def parse[A](what: String, thunk: => A): Either[VerificationResult, A] =
    try Right(thunk)
    catch
      case e: TptpMappingException => Left(parseError(what, e.getMessage))
      case e: TPTPParseException   => Left(parseError(what, e.getMessage))
      case _: StackOverflowError   => Left(parseError(what, "formula is nested too deeply to parse"))
      case e: Throwable            => Left(VerificationResult(SzsStatus.Unknown, s"could not read $what file: ${e.getMessage}"))

  private def parseError(what: String, message: String): VerificationResult =
    VerificationResult(SzsStatus.VerifiedBad, s"$what parse error: $message")

  private final class Run(proof: ProofFile, problem: Option[ProblemFile], strictNegatedConjecture: Boolean):
    private val steps: List[Step]                  = proof.steps
    private val findings: mutable.ListBuffer[Finding] = mutable.ListBuffer.empty[Finding]

    /** The problem's steps by name (all leaves are assumed to reference the same
      * problem file); carries the role so leaf checks can require it. */
    private val problemSteps: Map[String, Step] =
      problem.fold(Map.empty[String, Step])(_.steps.map(s => s.name -> s).toMap)

    /** Symbol → the step that first introduced it, for the Skolem-freshness check.
      * Seeded with every symbol of a non-derived leaf, so a fresh Skolem symbol can
      * collide with none: the proof's own axioms and conjecture — always present,
      * and the only source when no problem file is given — plus the problem's
      * formulas (which may include axioms the proof does not cite). Derived steps
      * are deliberately excluded; they legitimately carry Skolem symbols (the
      * introduction itself, and its downstream uses). It is then extended, in
      * topological order, with each skolemization's freshly introduced symbols. A
      * Skolem symbol is fresh iff it is absent here when its step is reached; it is
      * then added. This subsumes both freshness conditions — "not in the leaves"
      * (the seed) and "not introduced by an earlier skolemization" (the
      * accumulation) — and the stored step name lets a clash name the introducer. */
    private val introducedBy: mutable.Map[String, String] = mutable.Map.empty
    private val seedLeaves: List[Step] =
      steps.collect { case a: Step.Axiom => a; case c: Step.Conjecture => c }
        ++ problem.toList.flatMap(_.steps)
    for step <- seedLeaves; sym <- symbolsOf(step.formula) do
      introducedBy.getOrElseUpdate(sym, step.name)


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
      val byName: SeqMap[String, Step] = checkDAGBuildMap(freshSteps)
      if hasError then return result

      // Phase 2 — per-step verification (leaves against the problem, plain
      // inferences against their parents). The conjecture was set aside above.
      checkConjecture(conj)
      for step <- byName.values do
        checkAxiom(step)
        checkNegatedConjecture(step, conj)
        checkPlainInferenceStep(step, byName)
        checkSkolemization(step, byName)

      // Phase 3 — the proof must reach its goal.
      checkProvesConjectureOrFalse(conj, byName.values)
      result

    /** Record a definite violation (drives the result to `VerifiedBad`). */
    private def error(message: String, step: String): Unit =
      findings += Finding(Severity.Error, message, Some(step))

    private def error(message: String): Unit =
      findings += Finding(Severity.Error, message)

    /** Record that the prover did not answer in time on a step that had to be
      * checked (drives the result to `Timeout`, unless an error takes precedence). */
    private def timeout(message: String, step: String): Unit =
      findings += Finding(Severity.Timeout, message, Some(step))

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
      *
      * The result is a [[SeqMap]] (insertion-ordered): later phases that *iterate*
      * — e.g. the per-step loop driving the Skolem-symbol accumulator — depend on
      * this topological order, so it is part of the return type, not just an
      * implementation detail of how the map happens to be built.
      */
    private def checkDAGBuildMap(steps: List[Step]): SeqMap[String, Step] =
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
      SeqMap.from((ordered.toList ++ cycleMembers).map(s => s.name -> s))

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
      *
      * With no problem file, the proof's own axioms and conjecture *are* the
      * problem, so they are accepted as given — there is nothing to check them
      * against, and the proof stands on its derivation steps alone.
      */
    private def checkLeaf(role: String, name: String, formula: Formula, source: Option[FileSource], requireConjecture: Boolean): Unit =
      (problem, source) match
        case (None, _) =>
          () // no problem file: the leaf is taken as part of the (implicit) problem
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
      * conjecture, and its formula is the negation of the conjecture.
      *
      * How "is the negation" is decided depends on [[strictNegatedConjecture]]: in
      * strict mode the formula must be *exactly* `¬conjecture` up to
      * alpha-equivalence (a structural check — any normalization belongs in its own
      * step); in non-strict mode the prover decides `N ⟺ ¬C` instead, allowing a
      * normalized negation (e.g. an NNF push-in). Other step kinds are ignored.
      */
    private def checkNegatedConjecture(step: Step, conj: Step.Conjecture): Unit = step match
      case n: Step.NegatedConjecture =>
        if n.status != Status.Cth then
          error("negated_conjecture must have status cth", n.name)
        n.parents match
          case List(p) if p == conj.name =>
            val negation: Formula = Formula.Not(conj.formula)
            if strictNegatedConjecture then
              if !alphaEquivalent(n.formula, negation) then
                error("formula is not the negation of the conjecture (up to variable renaming)", n.name)
            else
              VampireBridge.isConsequence(Nil, Formula.Iff(n.formula, negation), timeoutMs = PlainStepTimeoutMs) match
                case Consequence.Yes     => ()
                case Consequence.No      => error("formula is not equivalent to the negation of the conjecture", n.name)
                case Consequence.Unknown => timeout(s"could not verify the negation of the conjecture (no prover answer within ${PlainStepTimeoutMs / 1000}s)", n.name)
          case List(p) =>
            error(s"negated_conjecture's parent '$p' is not the conjecture '${conj.name}'", n.name)
          case ps =>
            error(s"negated_conjecture must have exactly one parent (the conjecture), found ${ps.size}", n.name)
      case _ => ()

    /** Verify that a plain (free) inference step's formula is a first-order
      * logical consequence of its parents, using the external prover. Other step
      * kinds are not verified here. Parents are guaranteed to resolve, as this
      * phase only runs once [[checkDAGBuildMap]] has succeeded.
      *
      * In ProoVer a plain step is guaranteed to be an *easy* consequence (the
      * competition does not test the underlying solver). So we give the prover only
      * [[PlainStepTimeoutMs]]; if it has not decided by then, the step is too
      * complicated to be a legitimate plain inference and the proof is rejected
      * (an error, not a left-open obligation).
      */
    private def checkPlainInferenceStep(step: Step, byName: Map[String, Step]): Unit = step match
      case pi: Step.PlainInference =>
        val premises: List[Formula] = pi.parents.map(name => byName(name).formula)
        VampireBridge.isConsequence(premises, pi.formula, timeoutMs = PlainStepTimeoutMs) match
          case Consequence.Yes     => ()
          case Consequence.No      => error("formula is not a logical consequence of its parents", pi.name)
          case Consequence.Unknown => timeout(s"step too complicated to verify (no prover answer within ${PlainStepTimeoutMs / 1000}s)", pi.name)
      case _ => ()

    /** Verify a `skolemize` step (status `esa`). The transformation is checked
      * structurally — never with the prover, since the soundness-critical
      * direction (parent SAT ⟹ child SAT) is satisfiability-preservation, not
      * entailment. See `checkSkolemization.md` for the full property list.
      */
    private def checkSkolemization(step: Step, byName: Map[String, Step]): Unit = step match
      case s: Step.Skolemization =>
        if s.status != Status.Esa then
          error("skolemization must have status esa", s.name)
        else
          s.parents match
            case List(parentName) =>
              s.binding match
                case None                 => error("skolemization has no skolemize(V, T) record", s.name)
                case Some(Binding(v, t))  => checkSkolemizationBody(s, byName, parentName, v, t)
            case ps =>
              error(s"skolemization must have exactly one parent, found ${ps.size}", s.name)
      case _ => ()

    /** The freshness + dependency + substitution core, once the step is known to
      * be a well-formed single-parent `esa` skolemization with a binding. */
    private def checkSkolemizationBody(s: Step.Skolemization, byName: Map[String, Step], parentName: String, v: String, t: Term): Unit =
      val parentFormula: Formula = byName(parentName).formula
      t match
        case Term.App(f, termArgs) if termArgs.forall(_.isInstanceOf[Term.Var]) =>
          val args: List[String]     = termArgs.collect { case Term.Var(n) => n }
          val clashing: List[String] = s.newSymbols.filter(introducedBy.contains)
          if !s.newSymbols.contains(f) then
            error(s"new_symbols must declare the skolem symbol '$f'", s.name)
          else if clashing.nonEmpty then
            val where: String = clashing.map(sym => s"'$sym' (already introduced by '${introducedBy(sym)}')").mkString(", ")
            error(s"skolem symbol $where is not fresh", s.name)
          else
            s.newSymbols.foreach(sym => introducedBy(sym) = s.name)
            skolemReplace(parentFormula, v, t) match
              case Left(why) => error(s"invalid skolemization: $why", s.name)
              case Right((rebuilt, gov, freeBody)) =>
                val foreign: Set[String] = args.toSet -- gov
                val missing: Set[String] = gov.filter(u => freeBody(u) && !args.contains(u))
                if foreign.nonEmpty then
                  error(s"skolem term uses out-of-scope variable(s): ${foreign.toList.sorted.mkString(", ")}", s.name)
                else if missing.nonEmpty then
                  error(s"skolem term omits dependency on ${missing.toList.sorted.mkString(", ")}", s.name)
                else if !alphaEquivalent(rebuilt, s.formula) then
                  error("skolemized formula does not match the parent with the existential replaced", s.name)
        case _ =>
          error("skolem term must be f(x1, …, xn) over variables", s.name)

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
        if all.exists(_.severity == Severity.Error) then SzsStatus.VerifiedBad
        else if all.exists(_.severity == Severity.Timeout) then SzsStatus.Timeout
        else SzsStatus.VerifiedGood
      VerificationResult(status, describe(status, all), all)

    private def describe(status: SzsStatus, all: List[Finding]): String = status match
      case SzsStatus.VerifiedBad =>
        all.filter(_.severity == Severity.Error).map(_.toString).mkString("; ")
      case SzsStatus.Timeout =>
        all.filter(_.severity == Severity.Timeout).map(_.toString).mkString("; ")
      case SzsStatus.VerifiedGood =>
        s"${steps.size} step(s) verified"
      case SzsStatus.Unknown =>
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

  // -- Skolemization support (pure) -------------------------------------------

  /** Variables occurring in a term. */
  private def termVars(t: Term): Set[String] = t match
    case Term.Var(n)       => Set(n)
    case Term.App(_, args) => args.flatMap(termVars).toSet

  /** Free (unbound) variables of a formula. */
  private def freeVars(f: Formula): Set[String] = f match
    case Formula.Pred(_, args)        => args.flatMap(termVars).toSet
    case Formula.Eq(l, r)             => termVars(l) ++ termVars(r)
    case Formula.True | Formula.False => Set.empty
    case Formula.Not(g)               => freeVars(g)
    case Formula.And(a, b)            => freeVars(a) ++ freeVars(b)
    case Formula.Or(a, b)             => freeVars(a) ++ freeVars(b)
    case Formula.Implies(a, b)        => freeVars(a) ++ freeVars(b)
    case Formula.Iff(a, b)            => freeVars(a) ++ freeVars(b)
    case Formula.Forall(w, b)         => freeVars(b) - w
    case Formula.Exists(w, b)         => freeVars(b) - w

  /** Every function and predicate symbol name occurring in a formula (used for
    * the freshness of a Skolem symbol). */
  private def symbolsOf(f: Formula): Set[String] =
    def st(t: Term): Set[String] = t match
      case Term.Var(_)       => Set.empty
      case Term.App(n, args) => args.flatMap(st).toSet + n
    f match
      case Formula.Pred(n, args)        => args.flatMap(st).toSet + n
      case Formula.Eq(l, r)             => st(l) ++ st(r)
      case Formula.True | Formula.False => Set.empty
      case Formula.Not(g)               => symbolsOf(g)
      case Formula.And(a, b)            => symbolsOf(a) ++ symbolsOf(b)
      case Formula.Or(a, b)             => symbolsOf(a) ++ symbolsOf(b)
      case Formula.Implies(a, b)        => symbolsOf(a) ++ symbolsOf(b)
      case Formula.Iff(a, b)            => symbolsOf(a) ++ symbolsOf(b)
      case Formula.Forall(_, b)         => symbolsOf(b)
      case Formula.Exists(_, b)         => symbolsOf(b)

  private def freshName(base: String, avoid: Set[String]): String =
    if !avoid.contains(base) then base
    else Iterator.from(1).map(i => s"$base$i").find(!avoid.contains(_)).get

  /** Capture-avoiding substitution of the variable `v` by the term `t`. Descent
    * stops where `v` is re-bound (shadowed); a binder whose name would capture a
    * variable of `t` is alpha-renamed before descending. */
  private def subst(f: Formula, v: String, t: Term): Formula =
    val tVars: Set[String] = termVars(t)
    def st(term: Term): Term = term match
      case Term.Var(n)       => if n == v then t else term
      case Term.App(n, args) => Term.App(n, args.map(st))
    def quant(make: (String, Formula) => Formula, w: String, body: Formula): Formula =
      if w == v then make(w, body)                       // v re-bound below: shadowed
      else if tVars.contains(w) then                     // would capture a var of t: rename w
        val w2: String = freshName(w, tVars ++ freeVars(body) + v)
        make(w2, go(subst(body, w, Term.Var(w2))))
      else make(w, go(body))
    def go(f: Formula): Formula = f match
      case Formula.Pred(n, args)        => Formula.Pred(n, args.map(st))
      case Formula.Eq(l, r)             => Formula.Eq(st(l), st(r))
      case Formula.True                 => Formula.True
      case Formula.False                => Formula.False
      case Formula.Not(g)               => Formula.Not(go(g))
      case Formula.And(a, b)            => Formula.And(go(a), go(b))
      case Formula.Or(a, b)             => Formula.Or(go(a), go(b))
      case Formula.Implies(a, b)        => Formula.Implies(go(a), go(b))
      case Formula.Iff(a, b)            => Formula.Iff(go(a), go(b))
      case Formula.Forall(w, b)         => quant((x, y) => Formula.Forall(x, y), w, b)
      case Formula.Exists(w, b)         => quant((x, y) => Formula.Exists(x, y), w, b)
    go(f)

  /** Polarity of a subformula under the assertion that the whole formula is true.
    * `Both` marks a position reachable through `<=>` (no well-defined polarity). */
  private enum Pol:
    case Pos, Neg, Both
    def flip: Pol = this match
      case Pol.Pos  => Pol.Neg
      case Pol.Neg  => Pol.Pos
      case Pol.Both => Pol.Both

  /** Locate the *effectively existential* binder of `v` in `parent` — tracking
    * polarity and the governing (effectively universal) variables in scope — and
    * replace that `Q v. ψ` with `subst(ψ, v, t)`. Returns the rebuilt formula, the
    * governing set at the cut, and `freeVars(ψ)`; or a `Left` describing why the
    * variable cannot be skolemized here.
    */
  private def skolemReplace(parent: Formula, v: String, t: Term): Either[String, (Formula, Set[String], Set[String])] =
    // Each walk returns the rewritten formula and, if the target was found inside,
    // the (governing universals, freeVars(body)) recorded at the cut.
    type Found = Option[(Set[String], Set[String])]
    def bin(make: (Formula, Formula) => Formula, a: Formula, b: Formula, pa: Pol, pb: Pol, gov: Set[String]): Either[String, (Formula, Found)] =
      walk(a, pa, gov).flatMap { case (a2, fa) =>
        val rest: Either[String, (Formula, Found)] = if fa.isDefined then Right((b, None)) else walk(b, pb, gov)
        rest.map { case (b2, fb) => (make(a2, b2), fa.orElse(fb)) }
      }
    def binder(make: (String, Formula) => Formula, isForall: Boolean, w: String, b: Formula, pol: Pol, gov: Set[String]): Either[String, (Formula, Found)] =
      val effUniversal: Boolean   = (isForall && pol == Pol.Pos) || (!isForall && pol == Pol.Neg)
      val effExistential: Boolean = (isForall && pol == Pol.Neg) || (!isForall && pol == Pol.Pos)
      if w == v then
        if pol == Pol.Both then Left(s"variable '$v' occurs in ambiguous (<=>) polarity")
        else if effExistential then Right((subst(b, v, t), Some((gov, freeVars(b)))))
        else walk(b, pol, gov + w).map((b2, fnd) => (make(w, b2), fnd)) // universal shadow: target is deeper
      else
        walk(b, pol, if effUniversal then gov + w else gov).map((b2, fnd) => (make(w, b2), fnd))
    def walk(f: Formula, pol: Pol, gov: Set[String]): Either[String, (Formula, Found)] = f match
      case Formula.Not(g)        => walk(g, pol.flip, gov).map((g2, fnd) => (Formula.Not(g2), fnd))
      case Formula.And(a, b)     => bin((x, y) => Formula.And(x, y), a, b, pol, pol, gov)
      case Formula.Or(a, b)      => bin((x, y) => Formula.Or(x, y), a, b, pol, pol, gov)
      case Formula.Implies(a, b) => bin((x, y) => Formula.Implies(x, y), a, b, pol.flip, pol, gov)
      case Formula.Iff(a, b)     => bin((x, y) => Formula.Iff(x, y), a, b, Pol.Both, Pol.Both, gov)
      case Formula.Forall(w, b)  => binder((x, y) => Formula.Forall(x, y), isForall = true, w, b, pol, gov)
      case Formula.Exists(w, b)  => binder((x, y) => Formula.Exists(x, y), isForall = false, w, b, pol, gov)
      case other                 => Right((other, None))
    walk(parent, Pol.Pos, freeVars(parent)).flatMap {
      case (rebuilt, Some((gov, freeBody))) => Right((rebuilt, gov, freeBody))
      case (_, None)                        => Left(s"no existential variable '$v' to skolemize in the parent")
    }
