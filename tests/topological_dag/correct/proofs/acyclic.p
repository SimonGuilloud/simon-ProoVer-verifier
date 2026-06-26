% Proof    : ../problems/acyclic.p
fof(a1, axiom, p, file('acyclic.p', a1)).
fof(a2, axiom, q, file('acyclic.p', a2)).
fof(goal, conjecture, p, file('acyclic.p', goal)).
fof(s1, plain, p, inference(r, [status(thm)], [a1, a2])).
