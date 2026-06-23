package proover.verifier

import java.io.File

import proover.parser.TptpReader
import proover.syntax.{ProblemFile, ProofFile}

/** Runs the file-based proof corpus under `<repo>/tests/<name>/`.
  *
  * Layout (per test set `<name>`):
  * {{{
  *   tests/<name>/correct/problems/<case>.p   correct/proofs/<case>.p
  *   tests/<name>/wrong/problems/<case>.p     wrong/proofs/<case>.p
  * }}}
  * Each problem pairs (by filename) with the proof of the same name; `correct`
  * pairs must verify, `wrong` pairs must fail verification. A proof may have a
  * sibling `<case>.expected` file listing finding substrings (one per line) that
  * the result must contain — pinning *why* a wrong case fails. New cases are
  * added by dropping files in, and a new test set by adding a directory.
  */
class ProofCorpusTest extends munit.FunSuite:

  private val testsRoot: File = File("../tests")

  test("test corpus directory is present") {
    assert(testsRoot.isDirectory, s"missing corpus directory ${testsRoot.getAbsolutePath}")
  }

  for
    testSet            <- dirs(testsRoot)
    (category, status) <- List("correct" -> SzsStatus.Verified, "wrong" -> SzsStatus.FailedVerified)
    problemFile        <- tptpFiles(File(testSet, s"$category/problems"))
  do
    val proofFile: File    = File(testSet, s"$category/proofs/${problemFile.getName}")
    val expectedFile: File = File(testSet, s"$category/proofs/${problemFile.getName.stripSuffix(".p")}.expected")
    test(s"${testSet.getName} / $category / ${problemFile.getName}") {
      assert(proofFile.isFile, s"no matching proof for ${problemFile.getName}")
      val problem: ProblemFile = TptpReader.readProblem(problemFile)
      val proof: ProofFile     = TptpReader.readProof(proofFile)
      val result: VerificationResult = Verifier.verify(proof, Some(problem))
      assertEquals(result.status, status, s"${problemFile.getName}: ${result.description}")
      for expected <- expectations(expectedFile) do
        assert(
          result.findings.exists(_.toString.contains(expected)),
          s"${problemFile.getName}: expected a finding containing \"$expected\", got: ${result.findings.mkString("; ")}"
        )
    }

  private def dirs(parent: File): Array[File] =
    Option(parent.listFiles).getOrElse(Array.empty[File]).filter(_.isDirectory).sortBy(_.getName)

  private def tptpFiles(dir: File): Array[File] =
    Option(dir.listFiles).getOrElse(Array.empty[File]).filter(_.getName.endsWith(".p")).sortBy(_.getName)

  /** Expected finding substrings from a sibling `.expected` file (one per line;
    * blank lines and `#` comments ignored). Empty when the file is absent. */
  private def expectations(file: File): List[String] =
    if !file.isFile then Nil
    else
      val src: scala.io.BufferedSource = scala.io.Source.fromFile(file)
      try src.getLines().map(_.trim).filter(l => l.nonEmpty && !l.startsWith("#")).toList
      finally src.close()
