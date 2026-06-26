% Proof    : ../problems/proves_conjecture_with_negated.p
% Reaches the goal by deriving the conjecture q directly, even though a (valid)
% negated_conjecture step is also present and unused.
fof(a1, axiom, p, file('proves_conjecture_with_negated.p', a1)).
fof(a2, axiom, p => q, file('proves_conjecture_with_negated.p', a2)).
fof(c, conjecture, q, file('proves_conjecture_with_negated.p', c)).
fof(nc, negated_conjecture, ~q, inference(negated_conjecture, [status(cth)], [c])).
fof(s1, plain, q, inference(mp, [status(thm)], [a1, a2])).
