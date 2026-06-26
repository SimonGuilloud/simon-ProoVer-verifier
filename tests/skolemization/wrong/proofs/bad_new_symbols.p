% Proof    : ../problems/bad_new_symbols.p
fof(a1, axiom, ?[X]: ![Y]: r(X, Y), file('bad_new_symbols.p', a1)).
fof(c, conjecture, ?[X]: ![Y]: r(X, Y), file('bad_new_symbols.p', c)).
fof(neg, negated_conjecture, ~(?[X]: ![Y]: r(X, Y)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk, plain, ~(?[X]: r(X, sK0(X))), inference(skolemize, [status(esa), new_symbols(skolem, [sK9]), skolemize(Y, sK0(X))], [neg])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [a1, sk])).
