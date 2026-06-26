package proover.parser

import java.io.File
import proover.syntax.*

class TptpReaderTest extends munit.FunSuite:

  private val examples: File = File("../examples")
  private def problem(rel: String) = TptpReader.readProblem(File(examples, rel))
  private def proof(rel: String)   = TptpReader.readProof(File(examples, rel))

  test("problem file: leaves with no source") {
    val p: ProblemFile = problem("correct/Proofs/Problems/example1_c.p")
    assertEquals(p.fileName, "example1_c.p")
    assertEquals(p.steps.size, 2)
    p.steps match
      case List(a: Step.Axiom, c: Step.Conjecture) =>
        assertEquals(a.name, "a1")
        assertEquals(a.source, None)
        assertEquals(c.name, "c")
        assertEquals(c.source, None)
      case other => fail(s"unexpected steps: $other")
  }

  test("proof file: header, problem reference, leaf sources") {
    val pf: ProofFile = proof("correct/Proofs/example1_proof.p")
    assertEquals(pf.problemRef, Some("Problems/example1_c.p"))
    assert(pf.header.text.contains("ProoVer 2026"))

    val axiom: Step.Axiom = pf.steps.collectFirst { case a: Step.Axiom => a }.get
    assertEquals(axiom.source, Some(FileSource("Problems/example1_c.p", "a1")))

    val neg: Step.NegatedConjecture = pf.steps.collectFirst { case n: Step.NegatedConjecture => n }.get
    assertEquals(neg.status, Status.Cth)
    assertEquals(neg.parents, List("c"))
  }

  test("proof file: negated_conjecture status fixed to cth in example2") {
    val neg: Step.NegatedConjecture = proof("correct/Proofs/example2_proof.p").steps
      .collectFirst { case n: Step.NegatedConjecture => n }.get
    assertEquals(neg.status, Status.Cth)
  }

  test("proof file: plain inferences carry rule, status and parents") {
    val plains: List[Step.PlainInference] = proof("correct/Proofs/example2_proof.p").steps
      .collect { case p: Step.PlainInference => p }
    assert(plains.exists(p => p.rule == "instantiate" && p.status == Status.Thm))
    val horn: Step.PlainInference = plains.find(_.rule == "horn").get
    assertEquals(horn.parents, List("a2", "s1", "s2", "s3"))
  }

  test("proof file: skolemization fields") {
    val skos: List[Step.Skolemization] = proof("correct/Proofs/example3_proof.p").steps
      .collect { case s: Step.Skolemization => s }
    assertEquals(skos.size, 2)
    val bride: Step.Skolemization = skos.find(_.name == "bride").get
    assertEquals(bride.status, Status.Esa)
    assertEquals(bride.newSymbols, List("sK0"))
    assertEquals(bride.binding, Some(Binding("Bride", Term.App("sK0", List(Term.Var("Marriage"))))))
    assertEquals(bride.parents, List("marriage"))
  }

  test("formula mapping: quantifiers, equality, connectives") {
    import Formula.*, Term.*
    // ? [X] : ~(p(X) => ! [Y] : p(Y))   from example1
    val c: Step.Conjecture = proof("correct/Proofs/example1_proof.p").steps
      .collectFirst { case c: Step.Conjecture => c }.get
    assertEquals(
      c.formula,
      Exists("X", Not(Implies(Pred("p", List(Var("X"))), Forall("Y", Pred("p", List(Var("Y")))))))
    )
    // equality / disequality from the evil example 2 (~(g(f(a)) = f(g(a))))
    val neg: Step.NegatedConjecture = proof("incorrect/Proofs/example2_e_proof.p").steps
      .collectFirst { case n: Step.NegatedConjecture => n }.get
    assertEquals(neg.formula, Not(Eq(App("g", List(App("f", List(App("a", Nil))))), App("f", List(App("g", List(App("a", Nil))))))))
  }

  test("all example files parse without error") {
    def walk(d: File): List[File] =
      if d.isDirectory then d.listFiles.toList.flatMap(walk) else List(d)
    val files: List[File] = walk(examples).filter(_.getName.endsWith(".p"))
    assert(files.nonEmpty)
    files.foreach { f =>
      val isProblem: Boolean = f.getParentFile.getName == "Problems"
      try if isProblem then TptpReader.readProblem(f) else TptpReader.readProof(f)
      catch case e: Throwable => fail(s"failed to parse ${f.getPath}: ${e.getMessage}")
    }
  }
