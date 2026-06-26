% Proof    : ../problems/cycle.p
% Wrong: s1 and s2 depend on each other.
fof(s1, plain, p, inference(r, [status(thm)], [s2])).
fof(s2, plain, q, inference(r, [status(thm)], [s1])).
fof(goal, conjecture, p, file('cycle.p', goal)).
