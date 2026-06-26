% Proof    : ../problems/altered_body.p
fof(a1, axiom, ~(![Y]: p(Y)), file('altered_body.p', a1)).
fof(c, conjecture, ?[Y]: ~p(Y), file('altered_body.p', c)).
fof(neg, negated_conjecture, ~(?[Y]: ~p(Y)), inference(negated_conjecture, [status(cth)], [c])).
fof(sk, plain, ~r(sK0), inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemize(Y, sK0)], [a1])).
fof(bot, plain, $false, inference(resolve, [status(thm)], [sk, neg])).
