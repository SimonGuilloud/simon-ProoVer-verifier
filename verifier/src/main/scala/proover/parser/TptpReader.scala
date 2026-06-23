package proover.parser

import java.io.File
import scala.collection.mutable
import scala.io.Source as IOSource

import leo.modules.input.TPTPParser
import leo.datastructures.TPTP

import proover.syntax.*

/** Reads TPTP `.p` files into ProoVer's [[ProblemFile]] / [[ProofFile]]
  * structures, using Leo-III's parser for the heavy lifting and [[TptpMapper]]
  * for the AST mapping.
  */
object TptpReader:

  /** Parse a problem file (axioms + conjecture). Throws [[TptpMappingException]]
    * if the file contains a derived (non-leaf) step.
    */
  def readProblem(file: File): ProblemFile =
    readProblemContent(readFile(file), file.getName, file.getAbsolutePath)

  /** Parse a problem from an in-memory string (location defaults to the name). */
  def readProblemString(content: String, name: String = "problem.p"): ProblemFile =
    readProblemContent(content, name, name)

  private def readProblemContent(content: String, fileName: String, location: String): ProblemFile =
    val steps: List[Step] = parseSteps(content)
    steps.foreach {
      case _: Step.Axiom | _: Step.Conjecture => ()
      case other =>
        throw TptpMappingException(s"Problem file '$fileName' contains a non-leaf step '${other.name}'")
    }
    ProblemFile(fileName, location, Header.fromContent(content), steps, signatureOf(steps))

  /** Parse a proof file. The referenced problem is read from the `% Proof :`
    * header field, if present.
    */
  def readProof(file: File): ProofFile =
    readProofContent(readFile(file), file.getName, file.getAbsolutePath)

  /** Parse a proof from an in-memory string (location defaults to the name). */
  def readProofString(content: String, name: String = "proof.p"): ProofFile =
    readProofContent(content, name, name)

  private def readProofContent(content: String, fileName: String, location: String): ProofFile =
    val header: Header    = Header.fromContent(content)
    val steps: List[Step] = parseSteps(content)
    ProofFile(fileName, location, header, header.field("Proof"), steps, signatureOf(steps))

  /** Run Leo-III's parser and map every annotated FOF formula to a [[Step]]. */
  private def parseSteps(content: String): List[Step] =
    val problem: TPTP.Problem = TPTPParser.problem(content)
    if problem.includes.nonEmpty then
      throw TptpMappingException("include() statements are not supported")
    problem.formulas.toList.map {
      case fof: TPTP.FOFAnnotated => TptpMapper.step(fof)
      case other =>
        throw TptpMappingException(s"Only FOF formulas are supported, found ${other.formulaType} formula '${other.name}'")
    }

  /** Accumulate the signature: every function/predicate name with, per arity it
    * is used at, the annotated formula where that arity was first seen (constants
    * and propositions count as arity 0).
    */
  private def signatureOf(steps: List[Step]): Signature =
    val sig: mutable.Map[String, mutable.Map[Int, String]] = mutable.Map.empty
    def add(name: String, arity: Int, formulaName: String): Unit =
      val byArity: mutable.Map[Int, String] = sig.getOrElseUpdate(name, mutable.Map.empty)
      if !byArity.contains(arity) then byArity(arity) = formulaName
    def term(t: Term, in: String): Unit = t match
      case Term.Var(_)       => ()
      case Term.App(n, args) => add(n, args.size, in); args.foreach(term(_, in))
    def formula(f: Formula, in: String): Unit = f match
      case Formula.Pred(n, args) => add(n, args.size, in); args.foreach(term(_, in))
      case Formula.Eq(l, r)      => term(l, in); term(r, in)
      case Formula.True | Formula.False => ()
      case Formula.Not(g)        => formula(g, in)
      case Formula.And(a, b)     => formula(a, in); formula(b, in)
      case Formula.Or(a, b)      => formula(a, in); formula(b, in)
      case Formula.Implies(a, b) => formula(a, in); formula(b, in)
      case Formula.Iff(a, b)     => formula(a, in); formula(b, in)
      case Formula.Forall(_, b)  => formula(b, in)
      case Formula.Exists(_, b)  => formula(b, in)
    steps.foreach(s => formula(s.formula, s.name))
    sig.view.mapValues(_.toMap).toMap

  private def readFile(file: File): String =
    val src: scala.io.BufferedSource = IOSource.fromFile(file)
    try src.mkString
    finally src.close()
