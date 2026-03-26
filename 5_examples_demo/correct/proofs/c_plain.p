fof(s1, plain, ~p(f(f(a))), inference(negated_conjecture, [status(thm)], [a1])).
fof(s2, plain, p(a) => p(f(a)), inference(instantiate, [status(thm)], [a1])).
fof(s3, plain, p(f(a)) => p(f(f(a))), inference(instantiate, [status(thm)], [a1])).
fof(s4, plain, p(f(f(a))), inference(horn, [status(thm)], [a2, s1, s2])).
fof(s5, plain, $false, inference(consequence, [status(thm)], [s1, s4])).