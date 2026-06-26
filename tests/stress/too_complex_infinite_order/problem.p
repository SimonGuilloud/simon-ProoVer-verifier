fof(serial, axiom, ![X]: ?[Y]: lt(X,Y)).
fof(irref, axiom, ![X]: ~lt(X,X)).
fof(trans, axiom, ![X,Y,Z]: ((lt(X,Y) & lt(Y,Z)) => lt(X,Z))).
fof(c, conjecture, $false).
