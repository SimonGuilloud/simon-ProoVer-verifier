% Proof    : ../problems/not_negation.p
% Wrong: ~r is not the negation of the conjecture q.
fof(a1, axiom, p, file('not_negation.p', a1)).
fof(c, conjecture, q, file('not_negation.p', c)).
fof(nc, negated_conjecture, ~r, inference(negated_conjecture, [status(cth)], [c])).
fof(f1, plain, $false, inference(contra, [status(thm)], [a1, nc])).
