% Proof    : ../problems/03_affirming_consequent.p
% Wrong: affirming the consequent (q, p=>q does not give p).
fof(ax1, axiom, p => q, file('03_affirming_consequent.p', ax1)).
fof(ax2, axiom, q, file('03_affirming_consequent.p', ax2)).
fof(goal, conjecture, p, file('03_affirming_consequent.p', goal)).
fof(i1, plain, p, inference(bogus, [status(thm)], [ax1, ax2])).
