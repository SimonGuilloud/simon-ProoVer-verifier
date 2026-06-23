# Inference rules

## Allowed 
* Plain
* Negated conjecture
* Skolemization (Geoff's specified format:  https://tptp.org/UserDocs/QuickGuide/Derivations.html)

## Not allowed
* Everything else

## Spec
* Strict inferences should be correct
* Freshness local to a branch

## Specified rule details
* `skolemize` / status `esa`: introduces exactly one new Skolem symbol via
  `new_symbols(skolem, [sK])`; the Skolem term depends exactly on the
  universally quantified variables in scope.
* `negated_conjecture` / status `cth`: parent must be a `conjecture`; the
  formula must negate the parent.
* `axiom` / status `thm`: must carry a `file` directive referencing the problem
  file and axiom name, with a formula alpha-equivalent to the named formula.

## Verification
* Specified rules are verified internally by the proof checker.
* Unspecified (`plain`) steps marked `thm`/`cth` are validated by an external
  ATP, invoked only on the individual step together with its premises.
* Re-checking the whole conjecture as a single external call is forbidden.