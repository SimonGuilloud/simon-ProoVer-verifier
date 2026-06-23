package proover.verifier

/** General verifier / result behaviour that the file-based corpus
  * ([[ProofCorpusTest]]) cannot express (it only asserts Verified / FailedVerified).
  * Per-check proof cases live as problem/proof pairs under `tests/<name>/`.
  */
class VerifierTest extends munit.FunSuite, VerifierFixtures:

  test("without a problem file, leaf origins are left unverified (NotVerified)") {
    val r: VerificationResult = Verifier.verify(proof("""
      fof(a1, axiom, p(a), file('problem.p', a1)).
      fof(c, conjecture, p(a), file('problem.p', c)).
    """))
    assertEquals(r.status, SzsStatus.NotVerified)
    assertEquals(r.errors, Nil)
  }

  test("a symbol reused at two arities yields an arity warning (still Verified)") {
    val r: VerificationResult = Verifier.verify(
      proof("""
        fof(a1, axiom, p, file('problem.p', a1)).
        fof(a2, axiom, p(a), file('problem.p', a2)).
        fof(c, conjecture, p, file('problem.p', c)).
        fof(s1, plain, p, inference(x, [status(thm)], [a1, a2])).
      """),
      Some(problem("""
        fof(a1, axiom, p).
        fof(a2, axiom, p(a)).
        fof(c, conjecture, p).
      """))
    )
    assertEquals(r.status, SzsStatus.Verified)
    val warning: String = r.warnings.map(_.message).find(_.contains("inconsistent arities")).getOrElse("")
    assert(warning.contains("'a1'") && warning.contains("'a2'"), s"warning should name both formulas: $warning")
  }

  test("deriving $false without a negated_conjecture is accepted, with a warning") {
    val r: VerificationResult = Verifier.verify(
      proof("""
        fof(a1, axiom, p, file('problem.p', a1)).
        fof(a2, axiom, ~p, file('problem.p', a2)).
        fof(c, conjecture, q, file('problem.p', c)).
        fof(f1, plain, $false, inference(contra, [status(thm)], [a1, a2])).
      """),
      Some(problem("""
        fof(a1, axiom, p).
        fof(a2, axiom, ~p).
        fof(c, conjecture, q).
      """))
    )
    assertEquals(r.status, SzsStatus.Verified)
    assert(r.warnings.exists(_.message.contains("without a negated_conjecture")), r.warnings.toString)
  }

  test("a refutation with a negated_conjecture has no vacuous-proof warning") {
    val r: VerificationResult = Verifier.verify(
      proof("""
        fof(a1, axiom, p, file('problem.p', a1)).
        fof(c, conjecture, p, file('problem.p', c)).
        fof(nc, negated_conjecture, ~p, inference(negated_conjecture, [status(cth)], [c])).
        fof(f1, plain, $false, inference(contra, [status(thm)], [a1, nc])).
      """),
      Some(problem("""
        fof(a1, axiom, p).
        fof(c, conjecture, p).
      """))
    )
    assertEquals(r.status, SzsStatus.Verified)
    assert(!r.warnings.exists(_.message.contains("without a negated_conjecture")), r.warnings.toString)
  }

  test("an unsupported role is reported as an SZS Error naming the cause") {
    val r: VerificationResult = Verifier.verifyString("fof(x, weird_role, p).")
    assertEquals(r.status, SzsStatus.Error)
    assert(r.description.contains("weird_role"), r.description)
  }

  test("a TPTP syntax error is reported as an SZS Error, not thrown") {
    val r: VerificationResult = Verifier.verifyString("fof(x, axiom, p &).")
    assertEquals(r.status, SzsStatus.Error)
    assert(r.description.contains("proof parse error"), r.description)
  }

  test("a malformed problem file is attributed to the problem") {
    val r: VerificationResult = Verifier.verifyString(
      "fof(c, conjecture, p, file('p.p', c)).",
      Some("cnf(x, axiom, p).")
    )
    assertEquals(r.status, SzsStatus.Error)
    assert(r.description.contains("problem parse error"), r.description)
  }

  test("a pathologically deep formula is reported as an Error, not a crash") {
    val deep: String = "~" * 50000 + "p"
    val r: VerificationResult = Verifier.verifyString(s"fof(a1, axiom, $deep, file('p.p', a1)).")
    assertEquals(r.status, SzsStatus.Error)
  }

  test("verifyString verifies a well-formed proof normally") {
    val r: VerificationResult = Verifier.verifyString(
      """fof(a1, axiom, p, file('p.p', a1)).
         fof(c, conjecture, p, file('p.p', c)).
         fof(s1, plain, p, inference(x, [status(thm)], [a1])).""",
      Some("""fof(a1, axiom, p).
              fof(c, conjecture, p).""")
    )
    assertEquals(r.status, SzsStatus.Verified)
  }

  test("szsLine renders the SZS ontology word and description") {
    val r: VerificationResult = VerificationResult(SzsStatus.FailedVerified, "boom")
    assertEquals(r.szsLine("example1"), "% SZS status FailedVerified for example1 : boom")
  }
