% Proof    : ../problems/proves_neither.p
% Wrong: derives q (valid), but the conjecture is r and $false is never reached.
fof(ax1, axiom, p, file('proves_neither.p', ax1)).
fof(ax2, axiom, p => q, file('proves_neither.p', ax2)).
fof(goal, conjecture, r, file('proves_neither.p', goal)).
fof(i1, plain, q, inference(mp, [status(thm)], [ax1, ax2])).
