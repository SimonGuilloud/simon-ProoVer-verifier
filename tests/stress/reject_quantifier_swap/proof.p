% ?X!Y r does NOT follow from !Y?X r — this proof is INVALID and should be
% FailedVerified, but the prover returns Unknown so it is only NotVerified.
fof(a1, axiom, ! [Y] : ? [X] : r(X, Y), file('problem.p', a1)).
fof(c, conjecture, ? [X] : ! [Y] : r(X, Y), file('problem.p', c)).
fof(s1, plain, ? [X] : ! [Y] : r(X, Y), inference(bad, [status(thm)], [a1])).
