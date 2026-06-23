package proover.printer

import java.io.File
import java.nio.file.{Files, Path}

import leo.modules.input.TPTPParser
import leo.datastructures.TPTP

import proover.syntax.*
import proover.parser.{TptpMapper, TptpReader}

class PrinterTest extends munit.FunSuite:

  private val examples: File = File("../examples_demo")

  // Parse helpers that mirror the printer's targets.
  private def parseFormula(s: String): Formula =
    TptpMapper.formula(TPTPParser.fof(s))
  private def parseStep(s: String): Step =
    TPTPParser.annotated(s) match
      case fof: TPTP.FOFAnnotated => TptpMapper.step(fof)
      case other                  => fail(s"not a FOF formula: $other")

  // ---- formula round-trips (including parenthesisation edge cases) ----

  import Formula.*, Term.*
  private def p(n: String): Formula  = Pred(n, Nil)
  private val (a, b, c, d): (Formula, Formula, Formula, Formula) = (p("a"), p("b"), p("c"), p("d"))

  private val formulas: List[Formula] = List(
    And(a, And(b, c)),                 // right-assoc chain, no parens
    And(And(a, b), c),                 // left nesting needs parens
    Or(And(a, b), c),                  // mixed &/| needs parens
    And(a, Or(b, c)),
    Implies(a, Implies(b, c)),         // non-assoc needs parens
    Implies(And(a, b), c),
    Iff(Or(a, b), And(c, d)),
    Not(And(a, b)),
    Not(Not(a)),
    Forall("X", And(Pred("p", List(Var("X"))), Pred("q", List(Var("X"))))),
    Forall("X", Pred("p", List(Var("X")))),
    And(Forall("X", Pred("p", List(Var("X")))), Pred("q", Nil)),
    Exists("X", Not(Implies(Pred("p", List(Var("X"))), Forall("Y", Pred("p", List(Var("Y"))))))),
    Not(Eq(App("g", List(App("a", Nil))), App("f", Nil)))
  )

  test("toString uses the pretty-printer for terms, formulas and steps") {
    val t: Term = App("f", List(Var("X")))
    assertEquals(t.toString, Printer.term(t))
    val f: Formula = And(a, Or(b, c))
    assertEquals(f.toString, Printer.formula(f))
    assertEquals(s"$f", "a & (b | c)")
    val step: Step = TptpReader.readProof(File(examples, "correct/proof/example1_proof.p")).steps.head
    assertEquals(step.toString, Printer.step(step))
  }

  test("toStringRaw gives the structural rendering, recursing into nested values") {
    val t: Term = App("f", List(Var("X")))
    assertEquals(t.toStringRaw, "App(f, List(Var(X)))")

    val f: Formula = Not(And(Pred("p", Nil), Eq(App("a", Nil), App("b", Nil))))
    assertEquals(f.toStringRaw, "Not(And(Pred(p, List()), Eq(App(a, List()), App(b, List()))))")

    val neg: Step.NegatedConjecture = TptpReader.readProof(File(examples, "correct/proof/example1_proof.p")).steps
      .collectFirst { case n: Step.NegatedConjecture => n }.get
    assertEquals(neg.toStringRaw, s"NegatedConjecture(s1, ${neg.formula.toStringRaw}, Cth, List(c))")
    // toStringRaw differs from the (pretty) toString
    assertNotEquals(neg.toStringRaw, neg.toString)
  }

  test("formula round-trips: parse(print(f)) == f") {
    formulas.foreach { f =>
      assertEquals(parseFormula(Printer.formula(f)), f, s"failed on: ${Printer.formula(f)}")
    }
  }

  // ---- step and file round-trips over the real examples ----

  private def allProofFiles: List[File] =
    def walk(d: File): List[File] = if d.isDirectory then d.listFiles.toList.flatMap(walk) else List(d)
    walk(examples).filter(f => f.getName.endsWith(".p") && f.getParentFile.getName == "proof")

  test("step round-trips: parse(print(step)) == step for every example step") {
    allProofFiles.foreach { f =>
      TptpReader.readProof(f).steps.foreach { s =>
        assertEquals(parseStep(Printer.step(s)), s, s"failed in ${f.getName} on: ${Printer.step(s)}")
      }
    }
  }

  test("proof file round-trips: readProof(print(pf)) preserves steps, header and ref") {
    allProofFiles.foreach { f =>
      val pf: ProofFile = TptpReader.readProof(f)
      val tmp: Path = Files.createTempFile("proover", ".p")
      try
        val printed: String = Printer.proof(pf)
        assert(printed.linesIterator.contains("% SZS output end Proof"), s"missing end marker in ${f.getName}")
        Files.writeString(tmp, printed)
        val reparsed: ProofFile = TptpReader.readProof(tmp.toFile)
        assertEquals(reparsed.steps, pf.steps, s"steps differ for ${f.getName}")
        assertEquals(reparsed.header, pf.header, s"header differs for ${f.getName}")
        assertEquals(reparsed.problemRef, pf.problemRef, s"problemRef differs for ${f.getName}")
      finally Files.deleteIfExists(tmp)
    }
  }

  test("problem file round-trips: readProblem(print(pf)) preserves steps and header") {
    val pf: ProblemFile = TptpReader.readProblem(File(examples, "correct/problem/example1_c.p"))
    val tmp: Path = Files.createTempFile("proover", ".p")
    try
      Files.writeString(tmp, Printer.problem(pf))
      val reparsed: ProblemFile = TptpReader.readProblem(tmp.toFile)
      assertEquals(reparsed.steps, pf.steps)
      assertEquals(reparsed.header, pf.header)
    finally Files.deleteIfExists(tmp)
  }
