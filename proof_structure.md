# Proofs 
This file details the structure of the proofs used in the competition.

## Proof Structure 
* Leaves are the axioms and the negated conjecture.
* A proof is a directed acyclic graph.
* All proofs are proof by refutation, i.e., negating the conjecture, and ending at $false.
* A proof consists in a sequence of inference steps.
* Proofs will have ``reasonable'' granularity according to the judgement of the organizers and panel.
* There is no restriction of the order of the proof steps (non-sorted).

## Language
* Proofs in TSTP format: https://tptp.org/UserDocs/QuickGuide/Derivations.html.
* Proofs of FOF problems with axioms and a conjecture.
* All proof steps will have role axiom, conjecture, negated_conjecture, or plain.
* All proof steps will have status(thm).
* Some inference steps are specified [here](inference_rules.md), and all the other are free.
* No sequent calculus.

## Specified Proof Steps
* Skolemization (Geoff's specified format):  https://tptp.org/UserDocs/QuickGuide/Derivations.html
* NNF (how?)
* CNF (how?)