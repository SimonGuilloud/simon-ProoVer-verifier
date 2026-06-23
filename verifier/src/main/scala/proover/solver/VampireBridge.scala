package proover.solver

import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit

import proover.printer.Printer
import proover.syntax.Formula

/** Bridges ProoVer's FOL ([[proover.syntax]]) to the Vampire theorem prover, an
  * alternative to [[PrincessBridge]].
  *
  * Vampire is an external C++ binary, so a query is rendered as a TPTP file —
  * premises as `axiom`s, the conclusion as a `conjecture` (Vampire negates it
  * and refutes) — written to a temp file and passed to the prover. Free variables
  * are universally closed first, matching the Princess bridge. The SZS status in
  * Vampire's output is mapped to a [[Consequence]]:
  *
  *   - `Theorem` / `Unsatisfiable` / `ContradictoryAxioms` → [[Consequence.Yes]]
  *   - `CounterSatisfiable` / `Satisfiable`                → [[Consequence.No]]
  *   - anything else (timeout, gave up, missing/unreadable) → [[Consequence.Unknown]]
  *
  * The binary is the project-vendored build at `vendor/vampire/bin/vampire`
  * (relative to the working directory), overridable via the `VAMPIRE`
  * environment variable — it does not rely on a global installation.
  */
object VampireBridge:

  private val executable: String = sys.env.getOrElse("VAMPIRE", "vendor/vampire/bin/vampire")

  /** Whether the configured Vampire binary can actually be run. */
  def available: Boolean =
    try
      val process: Process = ProcessBuilder(executable, "--version").start()
      process.waitFor(10, TimeUnit.SECONDS) && process.exitValue == 0
    catch case _: Throwable => false

  /** Decide whether `conclusion` is a logical consequence of `premises`, giving
    * Vampire at most `timeoutMs` milliseconds.
    */
  def isConsequence(premises: List[Formula], conclusion: Formula, timeoutMs: Int = 5000): Consequence =
    val input: Path = Files.createTempFile("proover-vampire", ".p")
    val output: Path = Files.createTempFile("proover-vampire-out", ".txt")
    try
      Files.writeString(input, render(premises, conclusion))
      run(input, output, timeoutMs)
    catch case _: Throwable => Consequence.Unknown
    finally
      Files.deleteIfExists(input)
      Files.deleteIfExists(output)

  /** Render the query as a TPTP problem. */
  private def render(premises: List[Formula], conclusion: Formula): String =
    val axioms: List[String] = premises.zipWithIndex.map { (premise, i) =>
      s"fof(ax$i, axiom, ${Printer.formula(Closure.universalClosure(premise))})."
    }
    val goal: String = s"fof(goal, conjecture, ${Printer.formula(Closure.universalClosure(conclusion))})."
    (axioms :+ goal).mkString("\n") + "\n"

  private def run(input: Path, output: Path, timeoutMs: Int): Consequence =
    val seconds: Int = math.max(1, math.ceil(timeoutMs / 1000.0).toInt)
    val builder: ProcessBuilder = ProcessBuilder(executable, "--time_limit", seconds.toString, input.toString)
    builder.redirectErrorStream(true)
    builder.redirectOutput(output.toFile)
    val process: Process = builder.start()
    if !process.waitFor(seconds + 5L, TimeUnit.SECONDS) then
      process.destroyForcibly()
      Consequence.Unknown
    else
      parse(Files.readString(output))

  /** Map the SZS status word in Vampire's output to a [[Consequence]]. */
  private def parse(output: String): Consequence =
    val status: Option[String] = output.linesIterator
      .find(_.contains("SZS status"))
      .map(_.split("SZS status").last.trim.takeWhile(!_.isWhitespace))
    status match
      case Some("Theorem" | "Unsatisfiable" | "ContradictoryAxioms") => Consequence.Yes
      case Some("CounterSatisfiable" | "Satisfiable")                => Consequence.No
      case _                                                          => Consequence.Unknown
