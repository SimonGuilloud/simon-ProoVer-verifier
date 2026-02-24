%----Starting formula
fof(marriage, plain, 
    ! [Marriage] :
    ? [Bride] :
    ? [Groom] :
    in_love(Groom, Bride)).

%----Skolemize Bride
tff(sK0_defn, definition, 
    ! [Marriage : $i] :
    (sK0(Marriage) = (#[Bride : $i] :
    ? [Groom] :
    in_love(Groom, Bride))), introduced(definition, [new_symbols(skolem, [sK0])], [marriage])).

fof(bride, plain, 
    ! [Marriage] :
    ? [Groom] :
    in_love(Groom, sK0(Marriage)), inference(skolemize, [status(esa), skolemized(Bride), bind(Bride, sK0(Marriage))], [marriage])).

%----Skolemize Groom
tff(sK1_defn, definition, 
    ! [Marriage : $i] :
    (sK1(Marriage) = (#[Groom : $i] :
    in_love(Groom, sK0(Marriage)))), introduced(definition, [new_symbols(skolem, [sK1])], [bride])).

fof(groom, plain, 
    ! [Marriage] :
    in_love(sK1(Marriage), sK0(Marriage)), inference(skolemize, [status(esa), skolemized(Groom), bind(Groom, sK1(Marriage))], [bride])).
