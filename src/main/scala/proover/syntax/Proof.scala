package proover.syntax

/** SZS-style inference status carried by a proof step.
  *
  *   - [[Thm]] (`thm`): the formula is a logical consequence of its parents.
  *   - [[Esa]] (`esa`): equisatisfiable, used by `skolemize`.
  *   - [[Cth]] (`cth`): counter-theorem, used by `negated_conjecture`.
  */
enum Status:
  case Thm, Esa, Cth

/** Reference to a formula in the original problem file, i.e. the TPTP
  * `file('problem.p', formulaName)` annotation attached to leaves.
  */
case class FileSource(fileName: String, formulaName: String)

/** A `bind(Variable, term)` entry of a `skolemize` inference, recording which
  * existential variable was replaced by which Skolem term.
  */
case class Binding(variable: String, term: Term)

/** A generic TSTP "useful info" term, used for inference parameters that we do
  * not interpret with dedicated fields (the free part of an `inference(...)`
  * record). Examples: `status(thm)`, `new_symbols(skolem, [sK0])`, `[sK0]`.
  */
enum Info:
  /** A bare identifier or atom, e.g. `thm`, `skolem`. */
  case Atom(name: String)

  /** A functor applied to arguments, e.g. `status(thm)`. */
  case Compound(name: String, args: List[Info])

  /** A bracketed list, e.g. `[sK0]`. */
  case InfoList(items: List[Info])

/** A single proof step (a top-level `fof(...)` annotated formula).
  *
  * The step kind reflects the ProoVer guidelines rather than the raw TPTP role:
  * `skolemize` steps carry the TPTP role `plain` but are modelled as their own
  * [[Skolemization]] kind. Every step exposes its [[name]] (the formula label,
  * e.g. `s1`) and its [[formula]].
  */
sealed trait Step:
  def name: String
  def formula: Formula

  /** The cited steps that count as logical premises — DAG dependencies that must
    * resolve and precede this step. Equals the step's parents, except a
    * `negated_conjecture`'s parent is the (separately handled) conjecture, which
    * is not a premise; so its premises are empty. */
  def premises: List[String] = Nil

  /** Renders as a TPTP `fof(...).` line via the pretty-printer. `final` so the
    * case classes do not regenerate a synthetic `toString`. */
  final override def toString: String = proover.printer.Printer.step(this)

  /** The structural rendering the default case-class `toString` would give. */
  def toStringRaw: String = proover.printer.Printer.raw(this)

object Step:
  /** A leaf axiom (role `axiom`). In a proof file it carries the
    * `file('problem.p', name)` [[source]] linking it back to the problem; in a
    * problem file it is the definition itself and [[source]] is `None`.
    */
  case class Axiom(
      name: String,
      formula: Formula,
      source: Option[FileSource]
  ) extends Step

  /** The leaf conjecture (role `conjecture`). [[source]] is the problem-file
    * reference in a proof file, or `None` in a problem file.
    */
  case class Conjecture(
      name: String,
      formula: Formula,
      source: Option[FileSource]
  ) extends Step

  /** Negation of the conjecture (role `negated_conjecture`, status `cth`). Its
    * single parent must be the [[Conjecture]] step.
    */
  case class NegatedConjecture(
      name: String,
      formula: Formula,
      status: Status,
      parents: List[String]
  ) extends Step

  /** An unspecified ("free") inference (role `plain`), identified by its rule
    * name (e.g. `instantiate`, `horn`, `consequence`, `deduction`). Parameters
    * beyond the status are kept generically in [[parameters]].
    */
  case class PlainInference(
      name: String,
      formula: Formula,
      rule: String,
      status: Status,
      parents: List[String],
      parameters: List[Info]
  ) extends Step:
    override def premises: List[String] = parents

  /** A `skolemize` inference (role `plain`, status `esa`). Introduces the fresh
    * symbols in [[newSymbols]]; the existentially quantified variable and the
    * Skolem term that replaces it are recorded in [[binding]].
    */
  case class Skolemization(
      name: String,
      formula: Formula,
      status: Status,
      newSymbols: List[String],
      binding: Option[Binding],
      parents: List[String]
  ) extends Step:
    override def premises: List[String] = parents

/** The leading comment block of a TPTP file (the `% ...` preamble), kept
  * verbatim so it can be inspected or re-emitted.
  */
case class Header(lines: List[String]):
  /** The header as a single string. */
  def text: String = lines.mkString("\n")

  /** Value of a `% Field : value` header line, if present (the part after the
    * first colon). E.g. `field("Proof")` on `% Proof : ../p.p` yields `../p.p`.
    */
  def field(name: String): Option[String] =
    lines.iterator
      .map(_.stripPrefix("%").trim)
      .filter(l => l == name || l.startsWith(s"$name ") || l.startsWith(s"$name:"))
      .map(l => l.dropWhile(_ != ':').drop(1).trim)
      .find(_.nonEmpty)

object Header:
  /** Extract the preamble: the contiguous run of leading comment/blank lines
    * before the first formula.
    */
  def fromContent(content: String): Header =
    Header(content.linesIterator.takeWhile(l => l.trim.isEmpty || l.trim.startsWith("%")).toList)

/** The signature of a file: each function/predicate name mapped to, for every
  * arity it is used with, the name of the annotated formula where that arity was
  * first seen. A name appearing with more than one arity is used inconsistently.
  * Accumulated at parse time.
  */
type Signature = Map[String, Map[Int, String]]

/** A parsed problem file: its axioms and conjecture (as leaf [[Step]]s), plus
  * the file name, absolute location, header preamble, and [[Signature]].
  */
case class ProblemFile(
    fileName: String,
    location: String,
    header: Header,
    steps: List[Step],
    signature: Signature = Map.empty
)

/** A parsed proof file: the proof steps, the file name, absolute location,
  * header preamble, the referenced problem file (from the `% Proof :` header
  * field), and the [[Signature]]. Step order is unconstrained beyond being a
  * DAG, so steps are kept as given and resolved by [[Step.name]].
  */
case class ProofFile(
    fileName: String,
    location: String,
    header: Header,
    problemRef: Option[String],
    steps: List[Step],
    signature: Signature = Map.empty
)
