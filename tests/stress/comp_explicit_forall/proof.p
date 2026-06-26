% ! [X] p(X) is the same closed formula as the free-variable axiom p(X), but the
% leaf check sees Forall vs Pred and rejects it.
fof(ax, axiom, ! [X] : p(X), file('problem.p', ax)).
fof(c, conjecture, p(a), file('problem.p', c)).
fof(s1, plain, p(a), inference(inst, [status(thm)], [ax])).
