% add(o,X)=X is true in the standard model but not a first-order consequence of the two
% defining equations (it needs induction); the prover cannot settle it within the 3s
% budget, so the verifier rejects the step as too complicated to verify (Timeout).
fof(base, axiom, ![X]: add(X, o) = X, file('problem.p', base)).
fof(rec, axiom, ![X,Y]: add(X, s(Y)) = s(add(X, Y)), file('problem.p', rec)).
fof(c, conjecture, ![X]: add(o, X) = X, file('problem.p', c)).
fof(s1, plain, ![X]: add(o, X) = X, inference(induct, [status(thm)], [base, rec])).
