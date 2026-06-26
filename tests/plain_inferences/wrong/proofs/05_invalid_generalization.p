% Proof    : ../problems/05_invalid_generalization.p
% Wrong: cannot universally generalize from a single instance p(a).
fof(ax1, axiom, p(a), file('05_invalid_generalization.p', ax1)).
fof(goal, conjecture, ! [X] : p(X), file('05_invalid_generalization.p', goal)).
fof(i1, plain, ! [X] : p(X), inference(bogus, [status(thm)], [ax1])).
