package proover.parser

import leo.datastructures.TPTP
import leo.datastructures.TPTP.{
  GeneralTerm,
  MetaFunctionData,
  MetaVariable,
  NumberData,
  DistinctObjectData
}
import leo.datastructures.TPTP.FOF

import proover.syntax.{Formula as F, Term as T, *}

/** Raised when a TPTP file parses successfully but does not match the shape
  * ProoVer expects: an unknown role, a malformed inference record, an
  * unsupported connective, a missing status, etc. This is distinct from the
  * parser's own `TPTPParseException`, which signals a syntax error.
  */
final class TptpMappingException(message: String) extends RuntimeException(message)

/** Maps Leo-III's TPTP AST ([[leo.datastructures.TPTP]]) onto ProoVer's syntax
  * ([[proover.syntax]]). The entry point is [[step]]; [[formula]] and [[term]]
  * convert the FOF sub-language.
  */
object TptpMapper:

  // ---------------------------------------------------------------------------
  // Formulas and terms
  // ---------------------------------------------------------------------------

  /** Convert a FOF formula. Multi-variable quantifiers desugar to nested
    * single-variable ones; `!=` becomes `Not(Eq(..))`; `$true`/`$false` become
    * the constants. Derived connectives (`<=`, `<~>`, `~|`, `~&`) are expanded.
    */
  def formula(f: FOF.Formula): F = f match
    case FOF.AtomicFormula("$true", Seq())  => F.True
    case FOF.AtomicFormula("$false", Seq()) => F.False
    case FOF.AtomicFormula(p, args)         => F.Pred(p, args.map(term).toList)
    case FOF.Equality(l, r)                 => F.Eq(term(l), term(r))
    case FOF.Inequality(l, r)               => F.Not(F.Eq(term(l), term(r)))
    case FOF.UnaryFormula(_, body)          => F.Not(formula(body)) // only `~`
    case FOF.QuantifiedFormula(q, vars, body) =>
      val inner: F = formula(body)
      q.pretty match
        case "!"   => vars.foldRight(inner)((v, acc) => F.Forall(v, acc))
        case "?"   => vars.foldRight(inner)((v, acc) => F.Exists(v, acc))
        case other => throw TptpMappingException(s"Unsupported quantifier '$other'")
    case FOF.BinaryFormula(conn, l, r) =>
      val lf: F = formula(l)
      val rf: F = formula(r)
      conn.pretty match
        case "&"   => F.And(lf, rf)
        case "|"   => F.Or(lf, rf)
        case "=>"  => F.Implies(lf, rf)
        case "<="  => F.Implies(rf, lf)
        case "<=>" => F.Iff(lf, rf)
        case "<~>" => F.Not(F.Iff(lf, rf))
        case "~|"  => F.Not(F.Or(lf, rf))
        case "~&"  => F.Not(F.And(lf, rf))
        case other => throw TptpMappingException(s"Unsupported connective '$other'")

  /** Convert a FOF term. Distinct objects and numbers, which our term language
    * has no node for, are kept as opaque constants.
    */
  def term(t: FOF.Term): T = t match
    case FOF.AtomicTerm(f, args)  => T.App(f, args.map(term).toList)
    case FOF.Variable(name)       => T.Var(name)
    case FOF.DistinctObject(name) => T.App(name, Nil)
    case FOF.NumberTerm(value)    => T.App(value.pretty, Nil)

  // ---------------------------------------------------------------------------
  // Steps
  // ---------------------------------------------------------------------------

  /** Classify and convert one annotated FOF formula into a [[Step]]. The kind
    * follows the ProoVer guidelines: it is read from the role and, for `plain`
    * inferences, from the inference rule name (`skolemize` vs. everything else).
    */
  def step(af: TPTP.FOFAnnotated): Step =
    val form: F = af.formula match
      case FOF.Logical(f) => formula(f)
    af.role match
      case "axiom" | "hypothesis" | "lemma" | "definition" | "assumption" =>
        Step.Axiom(af.name, form, fileSource(af.annotations))
      case "conjecture" =>
        Step.Conjecture(af.name, form, fileSource(af.annotations))
      case "negated_conjecture" =>
        val inf: Inference = inference(af)
        Step.NegatedConjecture(af.name, form, inf.status, inf.parents)
      case "plain" =>
        val inf: Inference = inference(af)
        if inf.rule == "skolemize" then
          Step.Skolemization(af.name, form, inf.status, inf.newSymbols, inf.binding, inf.parents)
        else
          Step.PlainInference(af.name, form, inf.rule, inf.status, inf.parents, inf.parameters)
      case other =>
        throw TptpMappingException(s"Unsupported role '$other' on formula '${af.name}'")

  // ---------------------------------------------------------------------------
  // Annotation decoding
  // ---------------------------------------------------------------------------

  /** Decoded `inference(rule, [info...], [parents...])` source. */
  private case class Inference(
      rule: String,
      status: Status,
      parents: List[String],
      parameters: List[Info],
      newSymbols: List[String],
      binding: Option[Binding]
  )

  /** Extract a `file('problem.p', name)` source if the leaf has one. */
  private def fileSource(ann: TPTP.Annotations): Option[FileSource] =
    ann.flatMap { case (src, _) =>
      function(src) match
        case Some(("file", Seq(fileGt, nameGt))) =>
          for fn <- atom(fileGt); nm <- atom(nameGt) yield FileSource(fn, nm)
        case _ => None
    }

  /** Decode the `inference(...)` source of a derived step. */
  private def inference(af: TPTP.FOFAnnotated): Inference =
    val src: GeneralTerm = af.annotations
      .map(_._1)
      .getOrElse(throw TptpMappingException(s"Missing inference annotation on '${af.name}'"))
    function(src) match
      case Some(("inference", Seq(ruleGt, infoGt, parentsGt))) =>
        val rule: String = atom(ruleGt)
          .getOrElse(throw TptpMappingException(s"Malformed inference rule on '${af.name}'"))
        val infoItems: Seq[GeneralTerm] = infoGt.list.getOrElse(Seq.empty)
        val parents: List[String] = parentsGt.list.getOrElse(Seq.empty).flatMap(atom).toList
        val status: Status = statusOf(infoItems, af.name)
        val parameters: List[Info] =
          infoItems.filterNot(gt => functor(gt).contains("status")).map(info).toList
        Inference(rule, status, parents, parameters, newSymbolsOf(infoItems), skolemizeOf(infoItems))
      case _ =>
        throw TptpMappingException(s"Expected inference(...) source on '${af.name}'")

  // --- skolemize-specific fields ---

  /** `new_symbols(skolem, [sK0, ...])` -> the list of fresh symbol names. */
  private def newSymbolsOf(items: Seq[GeneralTerm]): List[String] =
    items.collectFirst {
      case Func("new_symbols", args) => args.lastOption.flatMap(_.list).getOrElse(Seq.empty).flatMap(atom).toList
    }.getOrElse(Nil)

  /** `skolemize(V, T)` -> the existentially quantified variable V and the Skolem
    * term T that replaces it, per the TPTP derivation format, as a [[Binding]].
    */
  private def skolemizeOf(items: Seq[GeneralTerm]): Option[Binding] =
    items.collectFirst {
      case Func("skolemize", Seq(v, t)) => atom(v).map(name => Binding(name, generalTerm(t)))
    }.flatten

  /** The `status(thm|esa|cth)` of an inference. Several `status(...)` annotations
    * on one inference are tolerated only if they all agree; a missing status, an
    * unrecognized status word, or conflicting statuses are all malformed. */
  private def statusOf(items: Seq[GeneralTerm], name: String): Status =
    val words: List[String] =
      items.collect { case Func("status", args) => args.headOption.flatMap(atom) }.flatten.toList
    words.distinct match
      case Nil        => throw TptpMappingException(s"Missing status on '$name'")
      case List(word) => parseStatus(word).getOrElse(throw TptpMappingException(s"unrecognized status '$word' on '$name'"))
      case many       => throw TptpMappingException(s"conflicting status annotations (${many.mkString(", ")}) on '$name'")

  private def parseStatus(word: String): Option[Status] = word match
    case "thm" => Some(Status.Thm)
    case "esa" => Some(Status.Esa)
    case "cth" => Some(Status.Cth)
    case _     => None

  // ---------------------------------------------------------------------------
  // General-term helpers
  // ---------------------------------------------------------------------------

  /** Matches a compound general term `functor(args...)`. */
  private object Func:
    def unapply(gt: GeneralTerm): Option[(String, Seq[GeneralTerm])] = gt match
      case GeneralTerm(Seq(MetaFunctionData(f, args)), None) => Some((f, args))
      case _                                                 => None

  /** The functor name of a compound general term, if any. */
  private def functor(gt: GeneralTerm): Option[String] = gt match
    case Func(f, _) => Some(f)
    case _          => None

  private def function(gt: GeneralTerm): Option[(String, Seq[GeneralTerm])] = Func.unapply(gt)

  /** A general term denoting a bare name/atom (functor with no args, a meta
    * variable, a distinct object, or a number).
    */
  private def atom(gt: GeneralTerm): Option[String] = gt match
    case GeneralTerm(Seq(MetaFunctionData(f, Seq())), None) => Some(unquote(f))
    case GeneralTerm(Seq(MetaVariable(v)), None)            => Some(v)
    case GeneralTerm(Seq(DistinctObjectData(n)), None)      => Some(n)
    case GeneralTerm(Seq(NumberData(n)), None)              => Some(n.pretty)
    case _                                                  => None

  /** Strip the surrounding single quotes of a TPTP single-quoted atom, e.g.
    * `'example1_c.p'` -> `example1_c.p` (the quotes are syntax, not the name).
    */
  private def unquote(s: String): String =
    if s.length >= 2 && s.startsWith("'") && s.endsWith("'") then s.substring(1, s.length - 1) else s

  /** Read a first-order [[Term]] out of a general term (used for `bind`'s
    * second argument, e.g. `sK0(Marriage)`).
    */
  private def generalTerm(gt: GeneralTerm): T = gt match
    case GeneralTerm(Seq(MetaVariable(v)), None)            => T.Var(v)
    case GeneralTerm(Seq(MetaFunctionData(f, args)), None)  => T.App(f, args.map(generalTerm).toList)
    case GeneralTerm(Seq(DistinctObjectData(n)), None)      => T.App(n, Nil)
    case GeneralTerm(Seq(NumberData(n)), None)              => T.App(n.pretty, Nil)
    case _ => throw TptpMappingException(s"Cannot read a term from '${gt.pretty}'")

  /** Convert an uninterpreted general term into a generic [[Info]] value. */
  private def info(gt: GeneralTerm): Info = gt.list match
    case Some(items) => Info.InfoList(items.map(info).toList)
    case None =>
      gt.data match
        case Seq(MetaFunctionData(f, Seq())) => Info.Atom(f)
        case Seq(MetaFunctionData(f, args))  => Info.Compound(f, args.map(info).toList)
        case Seq(MetaVariable(v))            => Info.Atom(v)
        case Seq(NumberData(n))              => Info.Atom(n.pretty)
        case Seq(DistinctObjectData(n))      => Info.Atom(n)
        case _                               => Info.Atom(gt.pretty)
