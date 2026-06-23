package proover.syntax

/** First-order logic terms.
  *
  * TPTP FOF terms are variables and function applications. A constant is simply
  * a function applied to no arguments (`App("a", Nil)`). Variables are the ones
  * bound by quantifiers; by TPTP convention they start with an upper-case letter
  * but we do not enforce that here.
  */
sealed trait Term:
  /** Renders as TPTP via the pretty-printer. `final` so the case classes do not
    * regenerate a synthetic `toString`. */
  final override def toString: String = proover.printer.Printer.term(this)

  /** The structural rendering the default case-class `toString` would give. */
  def toStringRaw: String = proover.printer.Printer.raw(this)

object Term:
  /** A (possibly quantified) variable, e.g. `X`, `Marriage`. */
  case class Var(name: String) extends Term

  /** A function symbol applied to arguments, e.g. `f(X)`, `sK0(Marriage)`, or a
    * constant `a` as `App("a", Nil)`.
    */
  case class App(name: String, args: List[Term]) extends Term

  /** Convenience constructor for a constant (nullary function). */
  def const(name: String): Term = App(name, Nil)

/** First-order logic formulas in TPTP FOF.
  *
  * Connectives are kept binary, mirroring the surface syntax tree. Equality has
  * its own node; disequality `!=` is represented as `Not(Eq(...))`. The TPTP
  * constants `$true` / `$false` are [[Formula.True]] / [[Formula.False]].
  */
sealed trait Formula:
  /** Renders as TPTP via the pretty-printer. `final` so the case classes do not
    * regenerate a synthetic `toString`. */
  final override def toString: String = proover.printer.Printer.formula(this)

  /** The structural rendering the default case-class `toString` would give. */
  def toStringRaw: String = proover.printer.Printer.raw(this)

object Formula:
  /** Atomic predicate application, e.g. `p(a)`, `in_love(Groom, Bride)`. A
    * propositional constant is `Pred("q", Nil)`.
    */
  case class Pred(name: String, args: List[Term]) extends Formula

  /** Equality atom `l = r`. */
  case class Eq(left: Term, right: Term) extends Formula

  /** The constant `$true`. */
  case object True extends Formula

  /** The constant `$false`. */
  case object False extends Formula

  case class Not(arg: Formula) extends Formula
  case class And(left: Formula, right: Formula) extends Formula
  case class Or(left: Formula, right: Formula) extends Formula
  case class Implies(left: Formula, right: Formula) extends Formula
  case class Iff(left: Formula, right: Formula) extends Formula

  /** Universal quantifier `! [variable] : body`. */
  case class Forall(variable: String, body: Formula) extends Formula

  /** Existential quantifier `? [variable] : body`. */
  case class Exists(variable: String, body: Formula) extends Formula
