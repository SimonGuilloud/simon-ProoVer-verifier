% Proof    : ../problems/08_transitivity.p
fof(ax1, axiom, ! [X, Y, Z] : ((edge(X, Y) & edge(Y, Z)) => edge(X, Z)), file('08_transitivity.p', ax1)).
fof(ax2, axiom, edge(a, b), file('08_transitivity.p', ax2)).
fof(ax3, axiom, edge(b, c), file('08_transitivity.p', ax3)).
fof(ax4, axiom, edge(c, d), file('08_transitivity.p', ax4)).
fof(goal, conjecture, ? [W] : (edge(a, W) & edge(a, c)), file('08_transitivity.p', goal)).
fof(i1, plain, (edge(a, b) & edge(b, c)) => edge(a, c), inference(inst, [status(thm)], [ax1])).
fof(i2, plain, edge(a, b) & edge(b, c), inference(conj, [status(thm)], [ax2, ax3])).
fof(i3, plain, edge(a, c), inference(mp, [status(thm)], [i1, i2])).
fof(i4, plain, (edge(a, c) & edge(c, d)) => edge(a, d), inference(inst, [status(thm)], [ax1])).
fof(i5, plain, edge(a, c) & edge(c, d), inference(conj, [status(thm)], [i3, ax4])).
fof(i6, plain, edge(a, d), inference(mp, [status(thm)], [i4, i5])).
fof(i7, plain, edge(a, d) & edge(a, c), inference(conj, [status(thm)], [i6, i3])).
fof(i8, plain, ? [W] : (edge(a, W) & edge(a, c)), inference(exists_intro, [status(thm)], [i7])).
