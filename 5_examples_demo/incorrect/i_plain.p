% SZS output start ListOfFormulae
fof(a1, axiom, ![X] : (f(f(X)) = f(g(X)) | g(f(X)) = f(f(X)))).
fof(c, conjecture, g(f(a)) = f(g(a))).
% SZS output end ListOfFormulae

% SZS output start Proof
fof(s1, plain, ~(g(f(a)) = f(g(a))), inference(negated_conjecture, [status(thm)], [c])).
fof(s2, plain, f(f(a)) = f(g(a)), inference(deduction, [status(thm)], [a1])).
fof(s3, plain, f(f(a)) = g(f(a)), inference(deduction, [status(thm)], [a1])).
fof(s4, plain, g(f(a)) = f(g(a)), inference(deduction, [status(thm)], [s2, s3])).
fof(s5, plain, $false, inference(deduction, [status(thm)], [s1, s4])).
% SZS output end Proof