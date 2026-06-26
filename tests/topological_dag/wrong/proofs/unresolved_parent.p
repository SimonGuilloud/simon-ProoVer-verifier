% Proof    : ../problems/unresolved_parent.p
% Wrong: parent 'ghost' does not exist.
fof(a1, axiom, p, file('unresolved_parent.p', a1)).
fof(s1, plain, p, inference(r, [status(thm)], [ghost])).
fof(goal, conjecture, p, file('unresolved_parent.p', goal)).
