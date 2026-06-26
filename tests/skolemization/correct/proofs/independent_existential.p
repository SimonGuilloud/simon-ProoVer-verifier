% Proof    : ../problems/independent_existential.p
fof(a1, axiom, ![X]: ?[Y]: p(Y), file('independent_existential.p', a1)).
fof(c, conjecture, ?[Y]: p(Y), file('independent_existential.p', c)).
fof(neg, negated_conjecture, ~(?[Y]: p(Y)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk, plain, ![X]: p(sK0), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0)], [a1])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [sk, neg])).
