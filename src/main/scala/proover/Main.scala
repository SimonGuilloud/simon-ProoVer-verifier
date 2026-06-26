package proover

import java.io.File

import proover.verifier.{SzsStatus, VerificationResult, Verifier}

/** Command-line entry point: verify a proof file and print its SZS status.
  *
  * {{{
  *   proover <proof.p> [--problem <problem.p>] [--strict-negated-conjecture=true|false]
  * }}}
  *
  * The proof file is required and positional. The problem file is optional, given
  * via `--problem` (or `--problem=...`); with no problem file the proof's own
  * axioms and conjecture are taken as the problem. `--strict-negated-conjecture`
  * (default `true`) controls how the negated_conjecture is checked: strict =
  * exactly `¬conjecture` up to alpha-equivalence; non-strict = the prover decides
  * `negated_conjecture ⟺ ¬conjecture`. The SZS line is printed to stdout and any
  * warnings to stderr; the exit code reflects the verdict (`VerifiedGood` → 0,
  * `VerifiedBad` → 1, `Timeout` → 2, `Unknown` → 3).
  */
object Main:

  private case class Options(proof: String, problem: Option[String], strictNegatedConjecture: Boolean)

  def main(args: Array[String]): Unit =
    parseArgs(args.toList) match
      case Left(usage) =>
        System.err.println(usage)
        sys.exit(2)
      case Right(opts) =>
        val result: VerificationResult =
          Verifier.verifyFile(File(opts.proof), opts.problem.map(File(_)), opts.strictNegatedConjecture)
        val name: String = File(opts.proof).getName.stripSuffix(".p")
        println(result.szsLine(name))
        result.warnings.foreach(w => System.err.println(s"% warning: $w"))
        sys.exit(exitCode(result.status))

  private val usage =
    "usage: proover <proof.p> [--problem <problem.p>] [--strict-negated-conjecture=true|false]"

  /** Parse the command line; returns a usage message on error. */
  private def parseArgs(args: List[String]): Either[String, Options] =
    def bool(value: String): Either[String, Boolean] = value match
      case "true"  => Right(true)
      case "false" => Right(false)
      case other   => Left(s"--strict-negated-conjecture expects true or false, got '$other'\n$usage")
    def loop(remaining: List[String], proof: Option[String], problem: Option[String], strict: Boolean): Either[String, Options] =
      remaining match
        case "--problem" :: p :: rest                => loop(rest, proof, Some(p), strict)
        case "--problem" :: Nil                      => Left(s"--problem requires a file argument\n$usage")
        case a :: rest if a.startsWith("--problem=") => loop(rest, proof, Some(a.stripPrefix("--problem=")), strict)
        case a :: rest if a.startsWith("--strict-negated-conjecture=") =>
          bool(a.stripPrefix("--strict-negated-conjecture=")).flatMap(b => loop(rest, proof, problem, b))
        case "--strict-negated-conjecture" :: v :: rest =>
          bool(v).flatMap(b => loop(rest, proof, problem, b))
        case a :: _ if a.startsWith("-")             => Left(s"unknown option '$a'\n$usage")
        case a :: rest if proof.isEmpty              => loop(rest, Some(a), problem, strict)
        case a :: _                                  => Left(s"unexpected extra argument '$a'\n$usage")
        case Nil                                     => proof.toRight(usage).map(Options(_, problem, strict))
    loop(args, None, None, strict = true)

  private def exitCode(status: SzsStatus): Int = status match
    case SzsStatus.VerifiedGood => 0
    case SzsStatus.VerifiedBad  => 1
    case SzsStatus.Timeout      => 2
    case SzsStatus.Unknown      => 3
