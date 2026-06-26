% Proof    : ../problems/nnf_negation.p
% REJECTED: the negated_conjecture must be the *syntactic* negation ~(?[X]: p(X)) up to
% alpha-equivalence. Writing its NNF !X ~p(X) here folds a normalization step into the
% negation; that transformation has to be its own step, so this is invalid.
fof(a1, axiom, p(a), file('nnf_negation.p', a1)).
fof(c, conjecture, ? [X] : p(X), file('nnf_negation.p', c)).
fof(nc, negated_conjecture, ! [X] : ~p(X), inference(negated_conjecture, [status(cth)], [c])).
fof(f1, plain, $false, inference(contra, [status(thm)], [a1, nc])).
