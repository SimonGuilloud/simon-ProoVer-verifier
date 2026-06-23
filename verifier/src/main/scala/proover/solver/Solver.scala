package proover.solver

import proover.syntax.{Formula, Term}

/** Whether a conclusion is a first-order logical consequence of some premises. */
enum Consequence:
  case Yes      // entailed: premises ∧ ¬conclusion is unsatisfiable
  case No       // not entailed: a countermodel exists
  case Unknown  // the solver could not decide (timeout / incompleteness)

/** Helpers shared by the prover bridges, so they agree on how formulas are read. */
private[solver] object Closure:

  /** Close a formula over its free variables (`∀` over each), so that free
    * variables are read as implicitly universally quantified.
    */
  def universalClosure(f: Formula): Formula =
    freeVars(f, Set.empty).foldLeft(f)((acc, v) => Formula.Forall(v, acc))

  private def freeVars(f: Formula, bound: Set[String]): Set[String] = f match
    case Formula.True | Formula.False => Set.empty
    case Formula.Pred(_, args)        => args.flatMap(freeVarsTerm(_, bound)).toSet
    case Formula.Eq(l, r)             => freeVarsTerm(l, bound) ++ freeVarsTerm(r, bound)
    case Formula.Not(g)               => freeVars(g, bound)
    case Formula.And(a, b)            => freeVars(a, bound) ++ freeVars(b, bound)
    case Formula.Or(a, b)             => freeVars(a, bound) ++ freeVars(b, bound)
    case Formula.Implies(a, b)        => freeVars(a, bound) ++ freeVars(b, bound)
    case Formula.Iff(a, b)            => freeVars(a, bound) ++ freeVars(b, bound)
    case Formula.Forall(v, b)         => freeVars(b, bound + v)
    case Formula.Exists(v, b)         => freeVars(b, bound + v)

  private def freeVarsTerm(t: Term, bound: Set[String]): Set[String] = t match
    case Term.Var(n)       => if bound(n) then Set.empty else Set(n)
    case Term.App(_, args) => args.flatMap(freeVarsTerm(_, bound)).toSet
