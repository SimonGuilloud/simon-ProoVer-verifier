% Proof    : ../problems/chain.p
fof(a1, axiom, ![X]: ?[Y]: ?[Z]: r(X, Y, Z), file('chain.p', a1)).
fof(c, conjecture, ?[X]: ?[Y]: ?[Z]: r(X, Y, Z), file('chain.p', c)).
fof(neg, negated_conjecture, ~(?[X]: ?[Y]: ?[Z]: r(X, Y, Z)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk1, plain, ![X]: ?[Z]: r(X, sK0(X), Z), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0(X))], [a1])).
fof(sk2, plain, ![X]: r(X, sK0(X), sK1(X)), inference(skolemize, [status(esa), new_symbols(skolem, [sK1]), skolemize(Z, sK1(X))], [sk1])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [sk2, neg])).
