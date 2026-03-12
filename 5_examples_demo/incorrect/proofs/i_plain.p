fof(s1, plain, f(f(a)) = f(g(a)), inference(deduction, [status(thm)], [a1])).
fof(s2, plain, f(f(a)) = g(f(a)), inference(deduction, [status(thm)], [a1])).
fof(s3, plain, g(f(a)) = f(g(a)), inference(deduction, [status(thm)], [s1, s2])).