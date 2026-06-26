% Proof    : ../problems/01_non_consequence.p
% Wrong: q does not follow from p alone.
fof(ax1, axiom, p, file('01_non_consequence.p', ax1)).
fof(goal, conjecture, q, file('01_non_consequence.p', goal)).
fof(i1, plain, q, inference(bogus, [status(thm)], [ax1])).
