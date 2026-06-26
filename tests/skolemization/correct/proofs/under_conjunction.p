% Proof    : ../problems/under_conjunction.p
fof(a1, axiom, ![X]: ( q(X) & ?[Y]: p(X, Y) ), file('under_conjunction.p', a1)).
fof(c, conjecture, ?[X]: ?[Y]: p(X, Y), file('under_conjunction.p', c)).
fof(neg, negated_conjecture, ~(?[X]: ?[Y]: p(X, Y)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk, plain, ![X]: ( q(X) & p(X, sK0(X)) ), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0(X))], [a1])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [sk, neg])).
