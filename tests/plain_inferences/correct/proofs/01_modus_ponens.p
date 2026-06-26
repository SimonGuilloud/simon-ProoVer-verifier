% Proof    : ../problems/01_modus_ponens.p
fof(ax1, axiom, p, file('01_modus_ponens.p', ax1)).
fof(ax2, axiom, p => q, file('01_modus_ponens.p', ax2)).
fof(goal, conjecture, q, file('01_modus_ponens.p', goal)).
fof(i1, plain, q, inference(mp, [status(thm)], [ax1, ax2])).
