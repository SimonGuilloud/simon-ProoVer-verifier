% Proof    : ../problems/proves_false.p
% Refutation: the axioms are contradictory, so $false is derived.
fof(ax1, axiom, p, file('proves_false.p', ax1)).
fof(ax2, axiom, ~p, file('proves_false.p', ax2)).
fof(goal, conjecture, q, file('proves_false.p', goal)).
fof(i1, plain, $false, inference(contradiction, [status(thm)], [ax1, ax2])).
