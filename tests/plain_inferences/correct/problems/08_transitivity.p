fof(ax1, axiom, ! [X, Y, Z] : ((edge(X, Y) & edge(Y, Z)) => edge(X, Z))).
fof(ax2, axiom, edge(a, b)).
fof(ax3, axiom, edge(b, c)).
fof(ax4, axiom, edge(c, d)).
fof(goal, conjecture, ? [W] : (edge(a, W) & edge(a, c))).
