% SZS output start ListOfFormulae
fof(a1, axiom, p(a)).
fof(c, conjecture, ![X] : (p(X))).
% SZS output end ListOfFormulae

% SZS output start Proof
fof(s1, negated_conjecture, ![X] : (~p(X)), inference(negated_conjecture, [status(cth)], [c])).
fof(f1, plain, $false, inference(consequence, [status(thm)], [s1, a1])).
% SZS output end Proof