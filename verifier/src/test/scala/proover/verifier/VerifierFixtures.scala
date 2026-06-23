package proover.verifier

import proover.parser.TptpReader
import proover.syntax.{ProblemFile, ProofFile}

/** Shared helpers for verifier tests: parse inline TPTP, assert on findings.
  * Mix into a `munit.FunSuite`. Kept deliberately tiny.
  */
trait VerifierFixtures:
  self: munit.Assertions =>

  def proof(tptp: String): ProofFile     = TptpReader.readProofString(tptp)
  def problem(tptp: String): ProblemFile = TptpReader.readProblemString(tptp)

  /** No error finding (the check under test accepts the proof). */
  def assertOk(r: VerificationResult): Unit =
    assert(r.errors.isEmpty, s"expected no errors, got: ${r.errors.mkString("; ")}")

  /** Some error finding contains `message` (and, if given, names `step`). */
  def assertError(r: VerificationResult, message: String, step: Option[String] = None): Unit =
    val hit: Boolean = r.findings.exists(f =>
      f.severity == Severity.Error && f.message.contains(message) && step.forall(f.step.contains))
    assert(hit, s"expected error containing \"$message\"${step.fold("")(s => s" at '$s'")}, got: ${r.findings.mkString("; ")}")
