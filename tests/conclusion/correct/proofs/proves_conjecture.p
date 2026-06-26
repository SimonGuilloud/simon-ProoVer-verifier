% Proof    : ../problems/proves_conjecture.p
fof(ax1, axiom, p, file('proves_conjecture.p', ax1)).
fof(ax2, axiom, p => q, file('proves_conjecture.p', ax2)).
fof(goal, conjecture, q, file('proves_conjecture.p', goal)).
fof(i1, plain, q, inference(mp, [status(thm)], [ax1, ax2])).
