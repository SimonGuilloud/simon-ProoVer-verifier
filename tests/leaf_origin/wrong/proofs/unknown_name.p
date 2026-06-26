% Proof    : ../problems/unknown_name.p
% Wrong: axiom references 'ghost', absent from the problem.
fof(ax, axiom, ! [X] : (p(X) => q(X)), file('unknown_name.p', ghost)).
fof(c, conjecture, q(a), file('unknown_name.p', c)).
