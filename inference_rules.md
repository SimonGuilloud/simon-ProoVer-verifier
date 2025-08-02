# Inference rules

## Allowed 
* Resolution
* Superposition
* Cut
* And
* Or
* Equality
* CNF
* Skolemization (Geoff's specified format:  https://tptp.org/UserDocs/QuickGuide/Derivations.html)
* Split
* Tseitin
* See more proof by E.

## Not Allowed
* No interferences such as Vampire's consistent_polarity_flip rule.

## Spec
* Strict inferences should be correct
* Freshness local to a branch