% Proof    : ../problems/06_mp_chain6.p
fof(ax0, axiom, p0, file('06_mp_chain6.p', ax0)).
fof(ax1, axiom, p0 => p1, file('06_mp_chain6.p', ax1)).
fof(ax2, axiom, p1 => p2, file('06_mp_chain6.p', ax2)).
fof(ax3, axiom, p2 => p3, file('06_mp_chain6.p', ax3)).
fof(ax4, axiom, p3 => p4, file('06_mp_chain6.p', ax4)).
fof(ax5, axiom, p4 => p5, file('06_mp_chain6.p', ax5)).
fof(ax6, axiom, p5 => p6, file('06_mp_chain6.p', ax6)).
fof(goal, conjecture, p6, file('06_mp_chain6.p', goal)).
fof(i1, plain, p1, inference(mp, [status(thm)], [ax0, ax1])).
fof(i2, plain, p2, inference(mp, [status(thm)], [i1, ax2])).
fof(i3, plain, p3, inference(mp, [status(thm)], [i2, ax3])).
fof(i4, plain, p4, inference(mp, [status(thm)], [i3, ax4])).
fof(i5, plain, p5, inference(mp, [status(thm)], [i4, ax5])).
fof(i6, plain, p6, inference(mp, [status(thm)], [i5, ax6])).
