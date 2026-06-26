% Proof    : ../problems/10_relational.p
fof(ax1, axiom, ! [X] : (dog(X) => animal(X)), file('10_relational.p', ax1)).
fof(ax2, axiom, ! [X] : (animal(X) => needs_food(X)), file('10_relational.p', ax2)).
fof(ax3, axiom, dog(rex), file('10_relational.p', ax3)).
fof(ax4, axiom, ! [X, Y] : ((needs_food(X) & owns(Y, X)) => feeds(Y, X)), file('10_relational.p', ax4)).
fof(ax5, axiom, owns(sam, rex), file('10_relational.p', ax5)).
fof(ax6, axiom, ! [X] : (feeds(sam, X) => kind(sam)), file('10_relational.p', ax6)).
fof(goal, conjecture, kind(sam) & ? [Z] : animal(Z), file('10_relational.p', goal)).
fof(i1, plain, dog(rex) => animal(rex), inference(inst, [status(thm)], [ax1])).
fof(i2, plain, animal(rex), inference(mp, [status(thm)], [i1, ax3])).
fof(i3, plain, animal(rex) => needs_food(rex), inference(inst, [status(thm)], [ax2])).
fof(i4, plain, needs_food(rex), inference(mp, [status(thm)], [i3, i2])).
fof(i5, plain, (needs_food(rex) & owns(sam, rex)) => feeds(sam, rex), inference(inst, [status(thm)], [ax4])).
fof(i6, plain, needs_food(rex) & owns(sam, rex), inference(conj, [status(thm)], [i4, ax5])).
fof(i7, plain, feeds(sam, rex), inference(mp, [status(thm)], [i5, i6])).
fof(i8, plain, feeds(sam, rex) => kind(sam), inference(inst, [status(thm)], [ax6])).
fof(i9, plain, kind(sam), inference(mp, [status(thm)], [i8, i7])).
fof(i10, plain, kind(sam) & ? [Z] : animal(Z), inference(combine, [status(thm)], [i9, i2])).
