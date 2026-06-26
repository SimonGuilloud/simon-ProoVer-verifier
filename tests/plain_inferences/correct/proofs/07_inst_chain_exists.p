% Proof    : ../problems/07_inst_chain_exists.p
fof(ax1, axiom, ! [X] : (p(X) => q(X)), file('07_inst_chain_exists.p', ax1)).
fof(ax2, axiom, ! [X] : (q(X) => r(X)), file('07_inst_chain_exists.p', ax2)).
fof(ax3, axiom, ! [X] : (r(X) => s(X)), file('07_inst_chain_exists.p', ax3)).
fof(ax4, axiom, p(a), file('07_inst_chain_exists.p', ax4)).
fof(goal, conjecture, ? [Y] : s(Y), file('07_inst_chain_exists.p', goal)).
fof(i1, plain, p(a) => q(a), inference(inst, [status(thm)], [ax1])).
fof(i2, plain, q(a), inference(mp, [status(thm)], [i1, ax4])).
fof(i3, plain, q(a) => r(a), inference(inst, [status(thm)], [ax2])).
fof(i4, plain, r(a), inference(mp, [status(thm)], [i3, i2])).
fof(i5, plain, r(a) => s(a), inference(inst, [status(thm)], [ax3])).
fof(i6, plain, s(a), inference(mp, [status(thm)], [i5, i4])).
fof(i7, plain, ? [Y] : s(Y), inference(exists_intro, [status(thm)], [i6])).
