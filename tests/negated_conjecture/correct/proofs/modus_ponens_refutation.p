% Proof    : ../problems/modus_ponens_refutation.p
fof(a1, axiom, p => q, file('modus_ponens_refutation.p', a1)).
fof(a2, axiom, p, file('modus_ponens_refutation.p', a2)).
fof(c, conjecture, q, file('modus_ponens_refutation.p', c)).
fof(nc, negated_conjecture, ~q, inference(negated_conjecture, [status(cth)], [c])).
fof(s1, plain, q, inference(mp, [status(thm)], [a1, a2])).
fof(f1, plain, $false, inference(contra, [status(thm)], [s1, nc])).
