package proover.solver

import scala.collection.mutable

import ap.SimpleAPI
import ap.SimpleAPI.ProverStatus
import ap.parser.{IAtom, IBoolLit, IFormula, IFunApp, IFunction, ITerm}
import ap.terfor.preds.Predicate
import ap.types.Sort

import proover.syntax.{Formula, Term}

/** Bridges ProoVer's FOL ([[proover.syntax]]) to the Princess theorem prover and
  * uses it to decide first-order logical consequence.
  *
  * Premises and conclusion are read as closed formulas — free variables are
  * universally quantified (their universal closure). To decide whether the
  * conclusion `C` follows from premises `P₁ … Pₙ`, the premises are asserted and
  * `C` is added as a *conclusion*: Princess negates it internally, switches to
  * validity mode, and answers `Valid` (entailed) or `Invalid` (a countermodel
  * exists). Everything is mapped onto a single uninterpreted sort with
  * uninterpreted functions and predicates, i.e. pure first-order logic with
  * equality.
  */
object PrincessBridge:

  /** Decide whether `conclusion` is a logical consequence of `premises`, giving
    * the prover at most `timeoutMs` milliseconds before giving up with
    * [[Consequence.Unknown]].
    */
  def isConsequence(premises: List[Formula], conclusion: Formula, timeoutMs: Int = 5000): Consequence =
    SimpleAPI.withProver { p =>
      // Prover-level creation registers the sort's theory (needed for models).
      val sort: Sort      = p.createUninterpretedSort("U")
      val tr: Translation = Translation(p, sort)

      for premise <- premises do
        p.addAssertion(tr.formula(Closure.universalClosure(premise), Map.empty))
      // The conjecture as a conclusion: Princess assumes it false and switches
      // to validity mode, answering Valid/Invalid.
      p.addConclusion(tr.formula(Closure.universalClosure(conclusion), Map.empty))

      p.checkSat(false)
      val status: ProverStatus.Value = p.getStatus(timeoutMs.toLong) match
        case ProverStatus.Running => p.stop
        case other                => other
      status match
        case ProverStatus.Valid   => Consequence.Yes
        case ProverStatus.Invalid => Consequence.No
        case _                    => Consequence.Unknown
    }

  // -- Translation to Princess ------------------------------------------------

  /** Translates ProoVer terms/formulas into Princess `IExpression`s, allocating
    * one Princess symbol per name in the prover `p` (a name's arity is assumed
    * consistent). Bound variables are threaded through `env`.
    */
  private final class Translation(p: SimpleAPI, sort: Sort):
    private val constants: mutable.Map[String, ITerm]      = mutable.Map.empty
    private val functions: mutable.Map[String, IFunction]  = mutable.Map.empty
    private val predicates: mutable.Map[String, Predicate] = mutable.Map.empty

    def formula(f: Formula, env: Map[String, ITerm]): IFormula = f match
      case Formula.True          => IBoolLit(true)
      case Formula.False         => IBoolLit(false)
      case Formula.Pred(n, args) => IAtom(predicate(n, args.size), args.map(term(_, env)))
      case Formula.Eq(l, r)      => term(l, env) === term(r, env)
      case Formula.Not(g)        => !formula(g, env)
      case Formula.And(a, b)     => formula(a, env) & formula(b, env)
      case Formula.Or(a, b)      => formula(a, env) | formula(b, env)
      case Formula.Implies(a, b) => formula(a, env) ===> formula(b, env)
      case Formula.Iff(a, b)     => formula(a, env) <===> formula(b, env)
      case Formula.Forall(v, b)  => sort.all(x => formula(b, env + (v -> x)))
      case Formula.Exists(v, b)  => sort.ex(x => formula(b, env + (v -> x)))

    def term(t: Term, env: Map[String, ITerm]): ITerm = t match
      case Term.Var(n)       => env.getOrElse(n, constant(n))
      case Term.App(n, Nil)  => constant(n)
      case Term.App(n, args) => IFunApp(function(n, args.size), args.map(term(_, env)))

    private def constant(n: String): ITerm =
      constants.getOrElseUpdate(n, p.createConstant(n, sort))
    private def function(n: String, arity: Int): IFunction =
      functions.getOrElseUpdate(n, p.createFunction(n, List.fill(arity)(sort), sort))
    private def predicate(n: String, arity: Int): Predicate =
      predicates.getOrElseUpdate(n, p.createRelation(n, List.fill(arity)(sort)))
