fof(ax1, axiom, ! [X] : (dog(X) => animal(X))).
fof(ax2, axiom, ! [X] : (animal(X) => needs_food(X))).
fof(ax3, axiom, dog(rex)).
fof(ax4, axiom, ! [X, Y] : ((needs_food(X) & owns(Y, X)) => feeds(Y, X))).
fof(ax5, axiom, owns(sam, rex)).
fof(ax6, axiom, ! [X] : (feeds(sam, X) => kind(sam))).
fof(goal, conjecture, kind(sam) & ? [Z] : animal(Z)).
