% Proof    : ../problems/foreign_var.p
fof(a1, axiom, ![X]: ?[Y]: p(X, Y), file('foreign_var.p', a1)).
fof(c, conjecture, ?[X]: ?[Y]: p(X, Y), file('foreign_var.p', c)).
fof(neg, negated_conjecture, ~(?[X]: ?[Y]: p(X, Y)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk, plain, ![X]: p(X, sK0(Z)), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0(Z))], [a1])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [sk, neg])).
