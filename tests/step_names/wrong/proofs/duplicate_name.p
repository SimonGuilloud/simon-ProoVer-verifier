% Proof    : ../problems/duplicate_name.p
% Wrong: two steps share the name a1.
fof(a1, axiom, p(a), file('duplicate_name.p', a1)).
fof(a1, plain, p(a), inference(r, [status(thm)], [a1])).
fof(goal, conjecture, p(a), file('duplicate_name.p', goal)).
