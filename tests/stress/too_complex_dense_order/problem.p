fof(irref, axiom, ![X]: ~lt(X,X)).
fof(trans, axiom, ![X,Y,Z]: ((lt(X,Y) & lt(Y,Z)) => lt(X,Z))).
fof(up, axiom, ![X]: ?[Y]: lt(X,Y)).
fof(dense, axiom, ![X,Y]: (lt(X,Y) => ?[Z]: (lt(X,Z) & lt(Z,Y)))).
fof(c, conjecture, $false).
