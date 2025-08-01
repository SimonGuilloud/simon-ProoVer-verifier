fof(f0, negated_conjecture, ~(p(a) & ? [X] : (p(X) => ! [Y] : (p(Y)))), inference(negated_conjecture, [status(cth)], [myproblem])).

cnf(f1, plain, ~p(a) | p(X0), inference(cnf, [status(thm)], [f0])).

cnf(f2, plain, p(X0), inference(res, [status(thm), 0, 0], [a1, f1])).

cnf(f3, plain, ~(p(a)) | ~(! [Y] : (p(Y))), inference(cnf, [status(thm)], [f0])).

cnf(f4, plain, ~(p(a)) | ~(p(f(X0))), inference(skolemization, [1, $fot(Y), $fot(f(X0)), status(esa)], [f3])).

cnf(f5, plain, ~(p(f(X0))), inference(res, [status(thm), 0, 0], [a1, f4])).

cnf(f6, plain, p(f(X0)), inference(instantiate, [status(thm), 0, $fot(X0), $fot(f(X0))], [f2])).

cnf(f7, plain, $false, inference(res, [status(thm), 0, 0], [f5, f6])).
