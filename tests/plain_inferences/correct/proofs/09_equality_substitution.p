% Proof    : ../problems/09_equality_substitution.p
fof(ax1, axiom, ! [X] : (q(X) => r(X)), file('09_equality_substitution.p', ax1)).
fof(ax2, axiom, ! [X] : (r(X) => s(X)), file('09_equality_substitution.p', ax2)).
fof(ax3, axiom, q(a), file('09_equality_substitution.p', ax3)).
fof(ax4, axiom, a = b, file('09_equality_substitution.p', ax4)).
fof(ax5, axiom, ! [X] : (s(X) => done(X)), file('09_equality_substitution.p', ax5)).
fof(ax6, axiom, ! [X] : (done(X) => finished(X)), file('09_equality_substitution.p', ax6)).
fof(goal, conjecture, finished(b), file('09_equality_substitution.p', goal)).
fof(i1, plain, q(a) => r(a), inference(inst, [status(thm)], [ax1])).
fof(i2, plain, r(a), inference(mp, [status(thm)], [i1, ax3])).
fof(i3, plain, r(a) => s(a), inference(inst, [status(thm)], [ax2])).
fof(i4, plain, s(a), inference(mp, [status(thm)], [i3, i2])).
fof(i5, plain, s(a) => done(a), inference(inst, [status(thm)], [ax5])).
fof(i6, plain, done(a), inference(mp, [status(thm)], [i5, i4])).
fof(i7, plain, done(a) => finished(a), inference(inst, [status(thm)], [ax6])).
fof(i8, plain, finished(a), inference(mp, [status(thm)], [i7, i6])).
fof(i9, plain, finished(b), inference(eq, [status(thm)], [ax4, i8])).
