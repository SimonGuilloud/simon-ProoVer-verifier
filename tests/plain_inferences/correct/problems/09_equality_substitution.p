fof(ax1, axiom, ! [X] : (q(X) => r(X))).
fof(ax2, axiom, ! [X] : (r(X) => s(X))).
fof(ax3, axiom, q(a)).
fof(ax4, axiom, a = b).
fof(ax5, axiom, ! [X] : (s(X) => done(X))).
fof(ax6, axiom, ! [X] : (done(X) => finished(X))).
fof(goal, conjecture, finished(b)).
