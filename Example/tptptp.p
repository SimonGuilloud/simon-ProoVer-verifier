fof(a1, axiom, p(a)).

fof(myproblem, conjecture, 
   p(a) & (? [X] :
    (p(X)
     => (! [Y] :
    (p(Y)))))).
