% Proof    : ../problems/02_mp_chain.p
fof(ax1, axiom, p, file('02_mp_chain.p', ax1)).
fof(ax2, axiom, p => q, file('02_mp_chain.p', ax2)).
fof(ax3, axiom, q => r, file('02_mp_chain.p', ax3)).
fof(goal, conjecture, r, file('02_mp_chain.p', goal)).
fof(i1, plain, q, inference(mp, [status(thm)], [ax1, ax2])).
fof(i2, plain, r, inference(mp, [status(thm)], [i1, ax3])).
