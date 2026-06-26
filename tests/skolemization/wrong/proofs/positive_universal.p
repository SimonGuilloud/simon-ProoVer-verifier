% Proof    : ../problems/positive_universal.p
fof(a1, axiom, ![X]: p(X), file('positive_universal.p', a1)).
fof(c, conjecture, p(a), file('positive_universal.p', c)).
fof(sk, plain, p(sK0), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(X, sK0)], [a1])).
fof(i1, plain, p(a), inference(instantiate, [status(thm)], [a1])).
