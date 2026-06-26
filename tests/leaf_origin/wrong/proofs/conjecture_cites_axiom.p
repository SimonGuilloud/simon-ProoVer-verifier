% Proof    : ../problems/conjecture_cites_axiom.p
% Wrong: the conjecture leaf cites the axiom 'ax'.
fof(a1, axiom, p(a), file('conjecture_cites_axiom.p', ax)).
fof(myconj, conjecture, p(a), file('conjecture_cites_axiom.p', ax)).
