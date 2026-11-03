package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.ServiceException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link PrietenieService}.
 */
public class PrietenieServiceImpl implements PrietenieService {
    private Repository<Long, Utilizator> repoUtilizatori;
    private Repository<Tuple<Long, Long>, Prietenie> repoPrietenii;

    /**
     * Creates the service.
     *
     * @param repoUtilizatori the users repository
     * @param repoPrietenii   the friendships repository
     */
    public PrietenieServiceImpl(Repository<Long, Utilizator> repoUtilizatori,
                            Repository<Tuple<Long, Long>, Prietenie> repoPrietenii) {
        this.repoUtilizatori = repoUtilizatori;
        this.repoPrietenii = repoPrietenii;
    }

    /** {@inheritDoc} */
    @Override
    public Prietenie adaugaPrietenie(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            throw new ValidationException("Id-urile utilizatorilor nu pot fi null.");
        }
        verificaExista(id1);
        verificaExista(id2);
        Prietenie p = new Prietenie(id1, id2);
        if (repoPrietenii.save(p) != null) {
            throw new ServiceException("Prietenia exista deja.");
        }
        return p;
    }

    /** {@inheritDoc} */
    @Override
    public Prietenie stergePrietenie(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            throw new ValidationException("Id-urile utilizatorilor nu pot fi null.");
        }
        Prietenie sters = repoPrietenii.delete(new Prietenie(id1, id2).getId());
        if (sters == null) {
            throw new EntityNotFoundException("Prietenia " + id1 + " - " + id2 + " nu exista.");
        }
        return sters;
    }

    /** {@inheritDoc} */
    @Override
    public List<Prietenie> getPrietenii() {
        List<Prietenie> rezultat = new ArrayList<Prietenie>();
        for (Prietenie p : repoPrietenii.findAll()) {
            rezultat.add(p);
        }
        return rezultat;
    }

    private void verificaExista(Long id) {
        if (repoUtilizatori.findOne(id) == null) {
            throw new EntityNotFoundException("Utilizatorul " + id + " nu exista.");
        }
    }
}
