package proover.solver

import proover.syntax.Formula.*
import proover.syntax.{Formula, Term}
import proover.syntax.Term.{App, Var}

class PrincessBridgeTest extends munit.FunSuite:

  private def pred(n: String, args: Term*): Formula = Pred(n, args.toList)
  private val a: Term = App("a", Nil)
  private val b: Term = App("b", Nil)

  test("assumes a non-empty domain (as standard FOL requires)") {
    // ∃X. X = X  and  ∀X p(X) ⊨ ∃X p(X)  are valid only if the domain is non-empty.
    assertEquals(PrincessBridge.isConsequence(Nil, Exists("X", Eq(Var("X"), Var("X")))), Consequence.Yes)
    assertEquals(
      PrincessBridge.isConsequence(List(Forall("X", pred("p", Var("X")))), Exists("X", pred("p", Var("X")))),
      Consequence.Yes
    )
  }

  test("modus ponens: p(a), ∀X. p(X) => q(X)  ⊨  q(a)") {
    val premises: List[Formula] = List(
      pred("p", a),
      Forall("X", Implies(pred("p", Var("X")), pred("q", Var("X"))))
    )
    assertEquals(PrincessBridge.isConsequence(premises, pred("q", a)), Consequence.Yes)
  }

  test("non-consequence: p(a)  ⊭  q(a)") {
    assertEquals(PrincessBridge.isConsequence(List(pred("p", a)), pred("q", a)), Consequence.No)
  }

  test("equality: a = b, p(a)  ⊨  p(b)") {
    val premises: List[Formula] = List(Eq(a, b), pred("p", a))
    assertEquals(PrincessBridge.isConsequence(premises, pred("p", b)), Consequence.Yes)
  }

  test("a disjunction does not entail one disjunct: p(a) | q(a)  ⊭  p(a)") {
    assertEquals(PrincessBridge.isConsequence(List(Or(pred("p", a), pred("q", a))), pred("p", a)), Consequence.No)
  }

  test("contradiction entails anything: a = b, ~(a = b)  ⊨  p(a)") {
    val premises: List[Formula] = List(Eq(a, b), Not(Eq(a, b)))
    assertEquals(PrincessBridge.isConsequence(premises, pred("p", a)), Consequence.Yes)
  }
