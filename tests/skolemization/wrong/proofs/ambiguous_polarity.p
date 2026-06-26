% Proof    : ../problems/ambiguous_polarity.p
fof(a1, axiom, (?[Y]: p(Y)) <=> q, file('ambiguous_polarity.p', a1)).
fof(a2, axiom, q, file('ambiguous_polarity.p', a2)).
fof(c, conjecture, q, file('ambiguous_polarity.p', c)).
fof(sk, plain, (p(sK0)) <=> q, inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0)], [a1])).
