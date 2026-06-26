package proover.verifier

import java.io.File

/** The verifier must always return a verdict, never crash. For every malformed
  * proof under a `tests/stress/error_<name>` directory, [[Verifier.verifyFile]]
  * should report an SZS `Error` (a parse/mapping failure or a caught stack
  * overflow) rather than throw. These exercise the `verifyFile` entry point, which
  * wraps parsing — unlike [[ProofCorpusTest]], which parses in the harness.
  */
class StressErrorTest extends munit.FunSuite:

  private val stressRoot: File = File("../tests/stress")

  private def errorDirs: Array[File] =
    Option(stressRoot.listFiles).getOrElse(Array.empty[File])
      .filter(d => d.isDirectory && d.getName.startsWith("error_"))
      .sortBy(_.getName)

  for dir <- errorDirs do
    test(s"stress / ${dir.getName} yields VerifiedBad") {
      val proofFile: File = File(dir, "proof.p")
      assert(proofFile.isFile, s"missing ${proofFile.getPath}")
      val r: VerificationResult = Verifier.verifyFile(proofFile)
      assertEquals(r.status, SzsStatus.VerifiedBad, s"${dir.getName}: ${r.description}")
      assert(r.description.nonEmpty, s"${dir.getName}: Error should carry a description")
    }
