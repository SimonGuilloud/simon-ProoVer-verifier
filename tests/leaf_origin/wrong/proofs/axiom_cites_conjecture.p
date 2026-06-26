% Proof    : ../problems/axiom_cites_conjecture.p
% Wrong: 'myax' is an axiom leaf but cites the problem's conjecture 'c'.
fof(myax, axiom, q(a), file('axiom_cites_conjecture.p', c)).
fof(goal, conjecture, q(a), file('axiom_cites_conjecture.p', c)).
