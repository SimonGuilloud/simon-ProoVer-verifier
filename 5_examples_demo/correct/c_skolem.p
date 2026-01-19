%----Starting formula
fof(marriage,plain,
    ! [Marriage] :
    ? [Bride] :
    ? [Groom] :
      in_love(Groom,Bride) ).

%----Skolemize Bride
fof(bride,plain,
    ! [Marriage] :
    ? [Groom] :
      in_love(Groom,sK0(Marriage)),
    inference(skolemize,[status(esa),new_symbols(skolem,[sK0]),skolemized(Bride),bind(Bride,sK0(Marriage))],[marriage]) ).
    
%----Skolemize Groom, new symbol sK1 recorded here.
fof(groom,plain,
    ! [Marriage] :
      in_love(sK1(Marriage),sK0(Marriage)),
    inference(skolemize,[status(esa),new_symbols(skolem,[sK1]),skolemized(Groom),bind(Groom,sK1(Marriage))],[bride]) ).