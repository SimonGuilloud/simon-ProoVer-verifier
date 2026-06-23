package proover.verifier

import java.io.File

import proover.parser.TptpReader

/** Adversarial stress tests demonstrating verifier holes. Each case under
  * `tests/stress/<name>/` has a `problem.p` + `proof.p`. These assertions pin the
  * CURRENT (buggy) verdict; the comment on each says what a correct verifier
  * SHOULD return. (Not auto-discovered by [[ProofCorpusTest]], which only looks
  * for `correct/` and `wrong/` subdirectories.)
  */
class StressTest extends munit.FunSuite:

  private def verdict(name: String): SzsStatus =
    val dir = File("../tests/stress", name)
    val problem = TptpReader.readProblem(File(dir, "problem.p"))
    val proof   = TptpReader.readProof(File(dir, "proof.p"))
    Verifier.verify(proof, Some(problem)).status

  // The alphaEquivalent-capture soundness exploit is now fixed and lives as a
  // regression case in tests/leaf_origin/wrong/axiom_shadowed_capture (run by
  // ProofCorpusTest). The cases below are remaining known/intended behaviours.

  // ---- COMPLETENESS: valid proofs that are wrongly rejected ----

  test("COMPLETENESS — free-variable rename in a leaf (should be Verified)") {
    // Y = Y vs X = X — the same axiom.
    assertEquals(verdict("comp_freevar_rename"), SzsStatus.FailedVerified) // BUG: rejected
  }

  test("COMPLETENESS — logically-equivalent goal q&p for p&q (should be Verified)") {
    assertEquals(verdict("comp_commuted_goal"), SzsStatus.FailedVerified) // BUG: rejected
  }

  test("COMPLETENESS — explicit ![X]p(X) vs free-variable p(X) (should be Verified)") {
    assertEquals(verdict("comp_explicit_forall"), SzsStatus.FailedVerified) // BUG: rejected
  }

  // ---- Fixed by switching the verifier's prover to Vampire ----

  test("a name reused at two arities (p/0 vs p/1) verifies — Princess crashed, Vampire is correct") {
    // p/0 and p/1 are distinct FOL symbols, so 'p' really does follow from the 'p' axiom.
    // The old Princess bridge conflated them by name and returned Error; Vampire verifies it.
    assertEquals(verdict("robust_arity_crash"), SzsStatus.Verified)
  }

  test("invalid quantifier-swap step is now refuted (FailedVerified)") {
    // ?X!Y r does not follow from !Y?X r. Princess returned Unknown (NotVerified);
    // Vampire saturates to CounterSatisfiable, so the verifier correctly rejects it.
    assertEquals(verdict("reject_quantifier_swap"), SzsStatus.FailedVerified)
  }
