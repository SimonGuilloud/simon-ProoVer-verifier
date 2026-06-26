% Proof    : ../problems/02_disjunction_to_disjunct.p
% Wrong: p does not follow from (p | q).
fof(ax1, axiom, p | q, file('02_disjunction_to_disjunct.p', ax1)).
fof(goal, conjecture, p, file('02_disjunction_to_disjunct.p', goal)).
fof(i1, plain, p, inference(bogus, [status(thm)], [ax1])).
