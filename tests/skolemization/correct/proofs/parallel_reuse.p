% Proof    : ../problems/parallel_reuse.p
fof(a1, axiom, ?[Y]: p(Y), file('parallel_reuse.p', a1)).
fof(a2, axiom, ![X]: (p(X) => r(X)), file('parallel_reuse.p', a2)).
fof(c, conjecture, ?[Y]: r(Y), file('parallel_reuse.p', c)).
fof(neg, negated_conjecture, ~(?[Y]: r(Y)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk, plain, p(sK0), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0)], [a1])).
fof(inst, plain, p(sK0) => r(sK0), inference(instantiate, [status(thm)], [a2])).
fof(r0, plain, r(sK0), inference(mp, [status(thm)], [sk, inst])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [r0, neg])).
