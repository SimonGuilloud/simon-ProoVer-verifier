% Proof    : ../problems/05_disjunction_weakening.p
fof(ax1, axiom, ! [X] : (a0(X) => a1(X)), file('05_disjunction_weakening.p', ax1)).
fof(ax2, axiom, ! [X] : (a1(X) => a2(X)), file('05_disjunction_weakening.p', ax2)).
fof(ax3, axiom, a0(c), file('05_disjunction_weakening.p', ax3)).
fof(goal, conjecture, a2(c) | a3(c), file('05_disjunction_weakening.p', goal)).
fof(i1, plain, a0(c) => a1(c), inference(inst, [status(thm)], [ax1])).
fof(i2, plain, a1(c), inference(mp, [status(thm)], [i1, ax3])).
fof(i3, plain, a1(c) => a2(c), inference(inst, [status(thm)], [ax2])).
fof(i4, plain, a2(c), inference(mp, [status(thm)], [i3, i2])).
fof(i5, plain, a2(c) | a3(c), inference(weaken, [status(thm)], [i4])).
