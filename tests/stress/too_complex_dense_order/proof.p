% A dense, transitive, irreflexive order with no top (the rationals) has only infinite
% models, so the prover cannot decide the step within the 3s budget; the verifier
% rejects it as too complicated to verify (Timeout).
fof(irref, axiom, ![X]: ~lt(X,X), file('problem.p', irref)).
fof(trans, axiom, ![X,Y,Z]: ((lt(X,Y) & lt(Y,Z)) => lt(X,Z)), file('problem.p', trans)).
fof(up, axiom, ![X]: ?[Y]: lt(X,Y), file('problem.p', up)).
fof(dense, axiom, ![X,Y]: (lt(X,Y) => ?[Z]: (lt(X,Z) & lt(Z,Y))), file('problem.p', dense)).
fof(c, conjecture, $false, file('problem.p', c)).
fof(f1, plain, $false, inference(deduce, [status(thm)], [irref, trans, up, dense])).
