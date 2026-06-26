% Derives q & p, which IS the conjecture p & q (commutativity), but the goal
% check needs an alpha-equivalent (not merely logically equivalent) step.
fof(a1, axiom, p, file('problem.p', a1)).
fof(a2, axiom, q, file('problem.p', a2)).
fof(c, conjecture, p & q, file('problem.p', c)).
fof(s1, plain, q & p, inference(conj, [status(thm)], [a1, a2])).
