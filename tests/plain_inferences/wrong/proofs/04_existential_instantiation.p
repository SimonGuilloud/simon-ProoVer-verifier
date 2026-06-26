% Proof    : ../problems/04_existential_instantiation.p
% Wrong: an existential witness need not be the constant a.
fof(ax1, axiom, ? [X] : p(X), file('04_existential_instantiation.p', ax1)).
fof(goal, conjecture, p(a), file('04_existential_instantiation.p', goal)).
fof(i1, plain, p(a), inference(bogus, [status(thm)], [ax1])).
