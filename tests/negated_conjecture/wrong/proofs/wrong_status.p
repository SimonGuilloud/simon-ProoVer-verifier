% Proof    : ../problems/wrong_status.p
% Wrong: negated_conjecture has status thm, must be cth.
fof(a1, axiom, p, file('wrong_status.p', a1)).
fof(c, conjecture, p, file('wrong_status.p', c)).
fof(nc, negated_conjecture, ~p, inference(negated_conjecture, [status(thm)], [c])).
fof(f1, plain, $false, inference(contra, [status(thm)], [a1, nc])).
