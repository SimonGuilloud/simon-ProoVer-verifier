fof(a1, axiom, ![X]: (p(X) => p(f(X)))).
fof(a2, axiom, p(a)).
fof(c, conjecture, p(f(f(a)))).

fof(s1, plain, p(a) => p(f(a)), inference(instantiate, [status(thm)], [a1])).
fof(s2, plain, p(f(a)) => p(f(f(a))), inference(instantiate, [status(thm)], [a1])).
fof(s3, plain, p(f(f(a))), inference(horn, [status(thm)], [a2, s1, s2])).