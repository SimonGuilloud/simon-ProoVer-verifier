package proover.solver

import proover.syntax.Formula.*
import proover.syntax.{Formula, Term}
import proover.syntax.Term.{App, Var}

class VampireBridgeTest extends munit.FunSuite:

  private def pred(n: String, args: Term*): Formula = Pred(n, args.toList)
  private def rr(x: Term, y: Term): Formula = Pred("r", List(x, y))
  private val a: Term = App("a", Nil)
  private val b: Term = App("b", Nil)

  /** The vendored Vampire binary; tests are skipped if it cannot be run. */
  private val vampireAvailable: Boolean = VampireBridge.available

  test("modus ponens: p(a), ∀X. p(X) => q(X)  ⊨  q(a)") {
    assume(vampireAvailable, "Vampire not available")
    val premises: List[Formula] = List(pred("p", a), Forall("X", Implies(pred("p", Var("X")), pred("q", Var("X")))))
    assertEquals(VampireBridge.isConsequence(premises, pred("q", a)), Consequence.Yes)
  }

  test("non-consequence: p(a)  ⊭  q(a)") {
    assume(vampireAvailable, "Vampire not available")
    assertEquals(VampireBridge.isConsequence(List(pred("p", a)), pred("q", a)), Consequence.No)
  }

  test("equality: a = b, p(a)  ⊨  p(b)") {
    assume(vampireAvailable, "Vampire not available")
    assertEquals(VampireBridge.isConsequence(List(Eq(a, b), pred("p", a)), pred("p", b)), Consequence.Yes)
  }

  test("a disjunction does not entail one disjunct: p(a) | q(a)  ⊭  p(a)") {
    assume(vampireAvailable, "Vampire not available")
    assertEquals(VampireBridge.isConsequence(List(Or(pred("p", a), pred("q", a))), pred("p", a)), Consequence.No)
  }

  test("contradiction entails anything: a = b, ~(a = b)  ⊨  p(a)") {
    assume(vampireAvailable, "Vampire not available")
    assertEquals(VampireBridge.isConsequence(List(Eq(a, b), Not(Eq(a, b))), pred("p", a)), Consequence.Yes)
  }

  test("assumes a non-empty domain: ∀X p(X) ⊨ ∃X p(X)") {
    assume(vampireAvailable, "Vampire not available")
    assertEquals(
      VampireBridge.isConsequence(List(Forall("X", pred("p", Var("X")))), Exists("X", pred("p", Var("X")))),
      Consequence.Yes
    )
  }

  test("quantifier swap is correctly refuted: ∀Y∃X r(X,Y)  ⊭  ∃X∀Y r(X,Y)") {
    // Princess returns Unknown here; Vampire saturates to CounterSatisfiable.
    assume(vampireAvailable, "Vampire not available")
    val premise: Formula = Forall("Y", Exists("X", rr(Var("X"), Var("Y"))))
    val conclusion: Formula = Exists("X", Forall("Y", rr(Var("X"), Var("Y"))))
    assertEquals(VampireBridge.isConsequence(List(premise), conclusion), Consequence.No)
  }
