fof(marriage,plain,
    ! [Marriage] :
    ? [Bride] :
    ? [Groom] :
      in_love(Groom,Bride) ).

fof(bride,plain,
    ! [Marriage] :
    ? [Groom] :
      in_love(Groom,sK0(Marriage)),
    inference(skolemize,[status(esa),skolemized(Bride),bind(Bride,sK0(Marriage))],[marriage]) ).
    
fof(groom,plain,
    ! [Marriage] :
      in_love(sK0(Marriage),sK0(Marriage)),
    inference(skolemize,[status(esa),new_symbols(skolem,[sK0]),skolemized(Groom),bind(Groom,sK0(Marriage))],[bride]) ).