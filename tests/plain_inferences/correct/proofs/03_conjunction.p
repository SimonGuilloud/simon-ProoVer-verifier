% Proof    : ../problems/03_conjunction.p
fof(ax1, axiom, p, file('03_conjunction.p', ax1)).
fof(ax2, axiom, q, file('03_conjunction.p', ax2)).
fof(ax3, axiom, p => r, file('03_conjunction.p', ax3)).
fof(ax4, axiom, q => s, file('03_conjunction.p', ax4)).
fof(goal, conjecture, r & s, file('03_conjunction.p', goal)).
fof(i1, plain, r, inference(mp, [status(thm)], [ax1, ax3])).
fof(i2, plain, s, inference(mp, [status(thm)], [ax2, ax4])).
fof(i3, plain, r & s, inference(conj, [status(thm)], [i1, i2])).
