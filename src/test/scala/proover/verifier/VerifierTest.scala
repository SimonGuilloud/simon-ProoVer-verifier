package proover.verifier

/** General verifier / result behaviour that the file-based corpus
  * ([[ProofCorpusTest]]) cannot express (it only asserts VerifiedGood / VerifiedBad).
  * Per-check proof cases live as problem/proof pairs under `tests/<name>/`.
  */
class VerifierTest extends munit.FunSuite, VerifierFixtures:

  test("without a problem file, the proof's own axioms and conjecture are accepted as the problem") {
    // No problem file: a1 and c are taken as given; the conjecture p(a) is one of the
    // axioms, so the goal is reached and the proof verifies on its own terms.
    val r: VerificationResult = Verifier.verify(proof("""
      fof(a1, axiom, p(a), file('problem.p', a1)).
      fof(c, conjecture, p(a), file('problem.p', c)).
    """))
    assertEquals(r.status, SzsStatus.VerifiedGood)
    assertEquals(r.findings, Nil)
  }

  test("without a problem file, a genuine derivation still verifies") {
    val r: VerificationResult = Verifier.verify(proof("""
      fof(a1, axiom, p, file('problem.p', a1)).
      fof(a2, axiom, p => q, file('problem.p', a2)).
      fof(c, conjecture, q, file('problem.p', c)).
      fof(s1, plain, q, inference(mp, [status(thm)], [a1, a2])).
    """))
    assertEquals(r.status, SzsStatus.VerifiedGood)
  }

  test("Skolem freshness is enforced against the proof's own axioms with no problem file") {
    // sK0 already occurs in axiom a2; the skolemization may not reuse it, even
    // though no problem file is provided to seed the freshness check from.
    val r: VerificationResult = Verifier.verify(proof("""
      fof(a1, axiom, ~(![Y]: p(Y)), file('p.p', a1)).
      fof(a2, axiom, q(sK0), file('p.p', a2)).
      fof(c, conjecture, ?[Y]: ~p(Y), file('p.p', c)).
      fof(neg, negated_conjecture, ~(?[Y]: ~p(Y)), inference(negated_conjecture, [status(cth)], [c])).
      fof(sk, plain, ~p(sK0), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0)], [a1])).
      fof(bot, plain, $false, inference(resolve, [status(thm)], [sk, neg])).
    """))
    assertEquals(r.status, SzsStatus.VerifiedBad)
    assert(
      r.errors.exists(_.message.contains("already introduced by 'a2'")),
      s"freshness error should name the clashing axiom: ${r.errors.mkString("; ")}"
    )
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
    assertEquals(r.status, SzsStatus.VerifiedGood)
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
    assertEquals(r.status, SzsStatus.VerifiedGood)
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
    assertEquals(r.status, SzsStatus.VerifiedGood)
    assert(!r.warnings.exists(_.message.contains("without a negated_conjecture")), r.warnings.toString)
  }

  test("an unsupported role is reported as VerifiedBad naming the cause") {
    val r: VerificationResult = Verifier.verifyString("fof(x, weird_role, p).")
    assertEquals(r.status, SzsStatus.VerifiedBad)
    assert(r.description.contains("weird_role"), r.description)
  }

  test("a TPTP syntax error is reported as VerifiedBad, not thrown") {
    val r: VerificationResult = Verifier.verifyString("fof(x, axiom, p &).")
    assertEquals(r.status, SzsStatus.VerifiedBad)
    assert(r.description.contains("proof parse error"), r.description)
  }

  test("a malformed problem file is attributed to the problem") {
    val r: VerificationResult = Verifier.verifyString(
      "fof(c, conjecture, p, file('p.p', c)).",
      Some("cnf(x, axiom, p).")
    )
    assertEquals(r.status, SzsStatus.VerifiedBad)
    assert(r.description.contains("problem parse error"), r.description)
  }

  test("a pathologically deep formula is reported as VerifiedBad, not a crash") {
    val deep: String = "~" * 50000 + "p"
    val r: VerificationResult = Verifier.verifyString(s"fof(a1, axiom, $deep, file('p.p', a1)).")
    assertEquals(r.status, SzsStatus.VerifiedBad)
  }

  test("verifyString verifies a well-formed proof normally") {
    val r: VerificationResult = Verifier.verifyString(
      """fof(a1, axiom, p, file('p.p', a1)).
         fof(c, conjecture, p, file('p.p', c)).
         fof(s1, plain, p, inference(x, [status(thm)], [a1])).""",
      Some("""fof(a1, axiom, p).
              fof(c, conjecture, p).""")
    )
    assertEquals(r.status, SzsStatus.VerifiedGood)
  }

  test("negated_conjecture: NNF negation is rejected when strict, accepted when non-strict") {
    // s1 is the NNF of the negation of c (¬∃X¬φ ≡ ∀Xφ): logically ¬C, not syntactically.
    val proofSrc = """
      fof(a1, axiom, p(a) & ~p(b), file('p.p', a1)).
      fof(c, conjecture, ?[X] : ~(p(X) => ![Y] : (p(Y))), file('p.p', c)).
      fof(s1, negated_conjecture, ![X] : (p(X) => ![Y] : (p(Y))), inference(negated_conjecture, [status(cth)], [c])).
      fof(f1, plain, $false, inference(consequence, [status(thm)], [s1, a1])).
    """
    val problemSrc = """
      fof(a1, axiom, p(a) & ~p(b)).
      fof(c, conjecture, ?[X] : ~(p(X) => ![Y] : (p(Y)))).
    """
    assertEquals(Verifier.verifyString(proofSrc, Some(problemSrc)).status, SzsStatus.VerifiedBad)
    assertEquals(Verifier.verifyString(proofSrc, Some(problemSrc), strictNegatedConjecture = false).status, SzsStatus.VerifiedGood)
  }

  test("szsLine renders the SZS ontology word and description") {
    val r: VerificationResult = VerificationResult(SzsStatus.VerifiedBad, "boom")
    assertEquals(r.szsLine("example1"), "% SZS status VerifiedBad for example1 : boom")
  }
