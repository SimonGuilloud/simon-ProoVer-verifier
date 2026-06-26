% Proof    : ../problems/formula_mismatch.p
% Wrong: conjecture is q(b) but the problem has q(a).
fof(ax, axiom, ! [X] : (p(X) => q(X)), file('formula_mismatch.p', ax)).
fof(c, conjecture, q(b), file('formula_mismatch.p', c)).
