fof(a1, axiom, c=a | c=b | p(d)).
fof(a2, axiom, p(a)).
fof(a3, axiom, p(b)).
fof(c, conjecture, p(c)).

fof(s1, plain, c=a, inference(split, [status(thm), split(a1), branch(s1, s3, s5)], [a1])).
fof(s2, plain, p(c), inference(substitute, [status(thm)], [a2, s1])).
fof(s3, plain, c=b, inference(split, [status(thm), split(a1), branch(s1, s3, s5)], [a1])).
fof(s4, plain, p(c), inference(substitute, [status(thm)], [a3, s3])).
fof(s5, plain, p(d), inference(split, [status(thm), split(a1), branch(s1, s3, s5)], [a1])).

