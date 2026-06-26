% Proof    : ../problems/04_equality_instantiation.p
fof(ax1, axiom, a = b, file('04_equality_instantiation.p', ax1)).
fof(ax2, axiom, p(a), file('04_equality_instantiation.p', ax2)).
fof(ax3, axiom, p(b) => q(b), file('04_equality_instantiation.p', ax3)).
fof(ax4, axiom, ! [X] : (q(X) => r(X)), file('04_equality_instantiation.p', ax4)).
fof(goal, conjecture, r(b), file('04_equality_instantiation.p', goal)).
fof(i1, plain, p(b), inference(eq, [status(thm)], [ax1, ax2])).
fof(i2, plain, q(b), inference(mp, [status(thm)], [i1, ax3])).
fof(i3, plain, q(b) => r(b), inference(inst, [status(thm)], [ax4])).
fof(i4, plain, r(b), inference(mp, [status(thm)], [i3, i2])).
