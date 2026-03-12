%----Negate conjecture: nobody is in love
fof(neg_c, negated_conjecture, 
    ~(? [X] :
    ? [Y] :
    in_love(X, Y)), inference(negated_conjecture, [status(cth)], [c])).

%----Skolemize Bride 
fof(bride,plain,
    ! [Marriage] :
    ? [Groom] :
      in_love(Groom,sK0(Marriage)),
    inference(skolemize, [status(esa), new_symbols(skolem, [sK0]), skolemized(Bride), bind(Bride, sK0(Marriage))], [marriage])).

%----Skolemize Groom     
fof(groom,plain,
    ! [Marriage] :
      in_love(sK0(Marriage),sK0(Marriage)),
    inference(skolemize,[status(esa), new_symbols(skolem, [sK0]), skolemized(Groom), bind(Groom, sK0(Marriage))], [bride])).

%----Instantiate at the known marriage m0
fof(groom_m0, plain, 
    in_love(sK0(m0), sK0(m0)), inference(instantiate, [status(thm)], [groom])).

%----Contradiction
fof(contradiction, plain, 
    $false,
    inference(consequence, [status(thm)], [neg_c, groom_m0])).    