% Proof    : ../problems/axiom_shadowed_capture.p
% Wrong: the axiom leaf claims ! [X] ! [Y] r(X,Y) (r everywhere), strictly stronger
% than the diagonal axiom it cites. Once let through by a buggy alpha-equivalence;
% now caught by the de Bruijn comparison.
fof(ax, axiom, ! [X] : ! [Y] : r(X, Y), file('axiom_shadowed_capture.p', ax)).
fof(c, conjecture, r(a, b), file('axiom_shadowed_capture.p', c)).
fof(s1, plain, r(a, b), inference(inst, [status(thm)], [ax])).
