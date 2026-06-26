% {serial, irreflexive, transitive} is consistent only in infinite models, so Vampire
% can neither refute it nor find a finite countermodel within the 3s plain-step budget;
% the verifier rejects the step as too complicated to verify (Timeout).
fof(serial, axiom, ![X]: ?[Y]: lt(X,Y), file('problem.p', serial)).
fof(irref, axiom, ![X]: ~lt(X,X), file('problem.p', irref)).
fof(trans, axiom, ![X,Y,Z]: ((lt(X,Y) & lt(Y,Z)) => lt(X,Z)), file('problem.p', trans)).
fof(c, conjecture, $false, file('problem.p', c)).
fof(f1, plain, $false, inference(deduce, [status(thm)], [serial, irref, trans])).
