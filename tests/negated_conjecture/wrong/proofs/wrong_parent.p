% Proof    : ../problems/wrong_parent.p
% Wrong: the negated_conjecture cites axiom a1, not the conjecture c.
fof(a1, axiom, p, file('wrong_parent.p', a1)).
fof(c, conjecture, p, file('wrong_parent.p', c)).
fof(nc, negated_conjecture, ~p, inference(negated_conjecture, [status(cth)], [a1])).
fof(f1, plain, $false, inference(contra, [status(thm)], [a1, nc])).
