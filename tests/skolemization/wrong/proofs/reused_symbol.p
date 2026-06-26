% Proof    : ../problems/reused_symbol.p
fof(a1, axiom, ~(![Y]: p(Y)), file('reused_symbol.p', a1)).
fof(a2, axiom, q(sK0), file('reused_symbol.p', a2)).
fof(c, conjecture, ?[Y]: ~p(Y), file('reused_symbol.p', c)).
fof(neg, negated_conjecture, ~(?[Y]: ~p(Y)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk, plain, ~p(sK0), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0)], [a1])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [sk, neg])).
