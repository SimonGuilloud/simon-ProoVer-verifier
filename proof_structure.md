# Proofs 
This file details the structure of the proofs used in the competition.

## Correct proof structure 
* Every leave is an axiom or a negated conjecture. Every axiom or negated conjecture must come from the input problem.
* A correct proof is a directed acyclic graph.
* All correct proofs are proof by refutation, i.e., ending at $false.
* If a proof is correct, adding correct steps to it keeps it correct.
* If a proof is correct, removing disconnected steps from it keeps it correct.
* Correct proofs will have ``reasonable'' granularity according to the judgement of the organizers and panel.
* There is no restriction of the order of the proof steps (non-sorted), as long as it is a DAG.

## Language
* Proofs in TSTP format: https://tptp.org/UserDocs/QuickGuide/Derivations.html.
* Proofs of FOF problems with axioms and a conjecture.
* All proofs must be syntactically well-formed TPTP.
* All proof steps will have role axiom, conjecture, negated_conjecture or plain.
* All proof steps will have status(thm), status(esa) or status(cth).
* The proof file is linked to its problem file via the `Proof:` header field,
  e.g. `% Proof    : path/to/problem_file.p`.
* Some inference steps are specified [here](inference_rules.md), and all the other are free.
* No sequent calculus.

## Specified Proof Steps
* Skolemization (Geoff's specified format):  https://tptp.org/UserDocs/QuickGuide/Derivations.html
  Rule name `skolemize`, status `esa`, introducing one new Skolem symbol via
  `new_symbols(skolem, [sK])`. The Skolem term must depend exactly on the
  universally quantified variables in scope.
* Negated conjecture: rule name `negated_conjecture`, status `cth`; the parent
  must be a `conjecture` and the formula must negate it (checked internally or
  via an external prover).
* Axiom: status `thm`, with a `file` directive referencing the problem file and
  axiom name, and a formula alpha-equivalent to the named formula.
* Plain (check with an external prover).