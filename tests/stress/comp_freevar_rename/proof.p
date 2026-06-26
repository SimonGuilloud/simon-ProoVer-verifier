% Y = Y is the SAME axiom as X = X (free vars are implicitly universal), but the
% leaf check requires the free-variable names to match.
fof(refl, axiom, Y = Y, file('problem.p', refl)).
fof(c, conjecture, a = a, file('problem.p', c)).
fof(s1, plain, a = a, inference(inst, [status(thm)], [refl])).
