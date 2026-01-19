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
      in_love(Marriage,sK0(Marriage)),
    inference(skolemize,[status(esa),new_symbols(skolem,[sK1]),skolemized(Groom),bind(Groom,sK1(Marriage))],[bride]) ).