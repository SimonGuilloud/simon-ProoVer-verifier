package proover.printer

import proover.syntax.*

/** Pretty-printer for ProoVer's syntax, emitting TPTP that the parser reads back
  * into the same structure: `parse(print(x)) == x`. Parenthesisation follows the
  * FOF grammar exactly — `&`/`|` are right-associative and cannot mix without
  * parentheses, `=>`/`<=>` are non-associative, and the bodies of quantifiers
  * and `~` must be unit formulas — so only the strictly necessary parentheses
  * are emitted.
  */
object Printer:

  // ---------------------------------------------------------------------------
  // Terms
  // ---------------------------------------------------------------------------

  def term(t: Term): String = t match
    case Term.Var(name)       => name
    case Term.App(name, Nil)  => name
    case Term.App(name, args) => s"$name(${args.map(term).mkString(", ")})"

  // ---------------------------------------------------------------------------
  // Formulas
  // ---------------------------------------------------------------------------

  def formula(f: Formula): String = f match
    case Formula.True          => "$true"
    case Formula.False         => "$false"
    case Formula.Pred(n, Nil)  => n
    case Formula.Pred(n, args) => s"$n(${args.map(term).mkString(", ")})"
    case Formula.Eq(l, r)      => s"${term(l)} = ${term(r)}"
    case Formula.Not(b)        => s"~${unit(b)}"
    case Formula.Forall(v, b)  => s"! [$v] : ${unit(b)}"
    case Formula.Exists(v, b)  => s"? [$v] : ${unit(b)}"
    case Formula.And(l, r)     => s"${operand(l, Assoc, left = true)} & ${operand(r, Assoc, left = false, sameAsParent = isAnd(r))}"
    case Formula.Or(l, r)      => s"${operand(l, Assoc, left = true)} | ${operand(r, Assoc, left = false, sameAsParent = isOr(r))}"
    case Formula.Implies(l, r) => s"${operand(l, NonAssoc)} => ${operand(r, NonAssoc)}"
    case Formula.Iff(l, r)     => s"${operand(l, NonAssoc)} <=> ${operand(r, NonAssoc)}"

  /** A formula usable as a unit (quantifier/`~` body, binary operand) without
    * extra parentheses: atoms, negations and quantified formulas.
    */
  private def isUnitary(f: Formula): Boolean = f match
    case _: Formula.And | _: Formula.Or | _: Formula.Implies | _: Formula.Iff => false
    case _                                                                     => true

  private def isAnd(f: Formula): Boolean = f.isInstanceOf[Formula.And]
  private def isOr(f: Formula): Boolean  = f.isInstanceOf[Formula.Or]

  /** Print a formula wrapped in parentheses iff it is not a unit formula. */
  private def unit(f: Formula): String =
    if isUnitary(f) then formula(f) else s"(${formula(f)})"

  private sealed trait Fixity
  private case object Assoc    extends Fixity // & and | : right-associative
  private case object NonAssoc extends Fixity // => and <=> : require parens around any binary operand

  /** Print an operand of a binary connective, parenthesising only when removing
    * the parentheses would change the parse:
    *   - non-associative parent: any binary operand needs parentheses;
    *   - associative parent: the left operand always needs parentheses if
    *     binary (the parser re-associates to the right); the right operand
    *     needs them unless it repeats the same connective (the chained case).
    */
  private def operand(f: Formula, fixity: Fixity, left: Boolean = false, sameAsParent: Boolean = false): String =
    val parenthesise: Boolean = fixity match
      case NonAssoc => !isUnitary(f)
      case Assoc    => if left then !isUnitary(f) else !isUnitary(f) && !sameAsParent
    if parenthesise then s"(${formula(f)})" else formula(f)

  // ---------------------------------------------------------------------------
  // Steps
  // ---------------------------------------------------------------------------

  /** Print one step as a single annotated `fof(...).` line. */
  def step(s: Step): String = s match
    case Step.Axiom(n, f, src)      => leaf(n, "axiom", f, src)
    case Step.Conjecture(n, f, src) => leaf(n, "conjecture", f, src)
    case Step.NegatedConjecture(n, f, status, parents) =>
      annotated(n, "negated_conjecture", f, inference("negated_conjecture", List(statusInfo(status)), parents))
    case Step.PlainInference(n, f, rule, status, parents, params) =>
      annotated(n, "plain", f, inference(rule, statusInfo(status) :: params.map(info), parents))
    case Step.Skolemization(n, f, status, newSymbols, skolemized, binding, parents) =>
      annotated(n, "plain", f, inference("skolemize", skolemizeInfos(status, newSymbols, skolemized, binding), parents))

  private def leaf(name: String, role: String, f: Formula, src: Option[FileSource]): String =
    src match
      case Some(FileSource(file, formulaName)) =>
        s"fof($name, $role, ${formula(f)}, file(${quoteAtom(file)}, ${quoteAtom(formulaName)}))."
      case None =>
        s"fof($name, $role, ${formula(f)})."

  private def annotated(name: String, role: String, f: Formula, source: String): String =
    s"fof($name, $role, ${formula(f)}, $source)."

  private def inference(rule: String, infos: List[String], parents: List[String]): String =
    s"inference($rule, [${infos.mkString(", ")}], [${parents.map(quoteAtom).mkString(", ")}])"

  private def statusInfo(status: Status): String =
    val s: String = status match
      case Status.Thm => "thm"
      case Status.Esa => "esa"
      case Status.Cth => "cth"
    s"status($s)"

  private def skolemizeInfos(
      status: Status,
      newSymbols: List[String],
      skolemized: Option[String],
      binding: Option[Binding]
  ): List[String] =
    List(statusInfo(status), s"new_symbols(skolem, [${newSymbols.mkString(", ")}])")
      ++ skolemized.map(v => s"skolemized($v)")
      ++ binding.map(b => s"bind(${b.variable}, ${term(b.term)})")

  /** Print a generic info / annotation term. */
  def info(i: Info): String = i match
    case Info.Atom(n)         => n
    case Info.Compound(n, as) => s"$n(${as.map(info).mkString(", ")})"
    case Info.InfoList(items) => s"[${items.map(info).mkString(", ")}]"

  // ---------------------------------------------------------------------------
  // Files
  // ---------------------------------------------------------------------------

  def proof(p: ProofFile): String     = file(p.header, p.steps)
  def problem(p: ProblemFile): String = file(p.header, p.steps)

  private def file(header: Header, steps: List[Step]): String =
    (header.lines ++ steps.map(step) ++ szsEndMarker(header)).mkString("\n") + "\n"

  /** The closing `% SZS output end <X>` marker matching the header's
    * `% SZS output start <X>` line (e.g. `Proof`, `ListOfFormulae`), if present.
    */
  private def szsEndMarker(header: Header): Option[String] =
    val Start = "SZS output start"
    header.lines.collectFirst {
      case l if l.contains(Start) => s"% SZS output end ${l.substring(l.indexOf(Start) + Start.length).trim}"
    }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private val LowerWord: scala.util.matching.Regex = "^[a-z][A-Za-z0-9_]*$".r

  /** Single-quote an atom that is not a TPTP lower-word (e.g. a file path like
    * `example1_c.p`), escaping quotes and backslashes; otherwise leave it bare.
    */
  def quoteAtom(s: String): String =
    if LowerWord.matches(s) then s
    else "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'"

  /** Structural rendering equivalent to the default case-class `toString` that
    * our overridden `toString` replaces. Recurses so nested values are shown
    * structurally too. Intended for debugging, exposed via `toStringRaw`.
    */
  def raw(value: Any): String = value match
    case null                              => "null"
    case s: String                         => s // case-class toString does not quote strings
    case None                              => "None"
    case Some(v)                           => s"Some(${raw(v)})"
    case xs: Seq[?]                        => xs.iterator.map(raw).mkString("List(", ", ", ")")
    case p: Product if p.productArity == 0 => p.productPrefix
    case p: Product                        => p.productIterator.map(raw).mkString(s"${p.productPrefix}(", ", ", ")")
    case other                             => other.toString

/** `x.pretty` extension syntax for every printable type. */
extension (t: Term) def pretty: String        = Printer.term(t)
extension (f: Formula) def pretty: String      = Printer.formula(f)
extension (s: Step) def pretty: String         = Printer.step(s)
extension (p: ProofFile) def pretty: String    = Printer.proof(p)
extension (p: ProblemFile) def pretty: String  = Printer.problem(p)
