# Proofs 
This file details the structure of the proofs used in the competition.

## Correct proof structure 
* Every leave is an axiom or a negated conjecture. Every axiom or negated conjecture must come from the input problem.
* A correct proof is a directed acyclic graph.
* All correct proofs are proof by refutation, i.e., ending at $false.
* If a proof is corrcect, adding correct steps to it keeps it correct.
* If a proof is correct, removing disconnected steps from it keeps it correct.
* Correct proofs will have ``reasonable'' granularity according to the judgement of the organizers and panel.
* There is no restriction of the order of the proof steps (non-sorted), as loong as it is a DAG.

## Language
* Proofs in TSTP format: https://tptp.org/UserDocs/QuickGuide/Derivations.html.
* Proofs of FOF problems with axioms and a conjecture.
* All proof steps will have role axiom, conjecture, negated_conjecture, esa or plain.
* All proof steps will have status(thm) or status(esa).
* Some inference steps are specified [here](inference_rules.md), and all the other are free.
* No sequent calculus.

## Specified Proof Steps
* Skolemization (Geoff's specified format):  https://tptp.org/UserDocs/QuickGuide/Derivations.html
* Plain (check with an external prover)
* Negated conjecture (check with an external prover)