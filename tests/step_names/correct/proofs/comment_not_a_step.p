% Proof    : ../problems/comment_not_a_step.p
fof(a1, axiom, p(a), file('comment_not_a_step.p', a1)).
%% fof(a1, axiom, p(b)) -- a comment, not a step
fof(c, conjecture, p(a), file('comment_not_a_step.p', c)).
