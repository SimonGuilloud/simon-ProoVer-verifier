% Uses 'p' at arity 0 and arity 1 within one inference; the translation asserts.
fof(a1, axiom, p, file('problem.p', a1)).
fof(a2, axiom, p(a), file('problem.p', a2)).
fof(c, conjecture, p, file('problem.p', c)).
fof(s1, plain, p, inference(x, [status(thm)], [a1, a2])).
