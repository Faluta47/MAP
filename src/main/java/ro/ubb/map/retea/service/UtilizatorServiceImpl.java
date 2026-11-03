package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link UtilizatorService}.
 */
public class UtilizatorServiceImpl implements UtilizatorService {
    private Repository<Long, Utilizator> repoUtilizatori;
    private Repository<Tuple<Long, Long>, Prietenie> repoPrietenii;

    /**
     * Creates the service.
     *
     * @param repoUtilizatori the users repository
     * @param repoPrietenii   the friendships repository (needed for the cascading delete)
     */
    public UtilizatorServiceImpl(Repository<Long, Utilizator> repoUtilizatori,
                             Repository<Tuple<Long, Long>, Prietenie> repoPrietenii) {
        this.repoUtilizatori = repoUtilizatori;
        this.repoPrietenii = repoPrietenii;
    }

    /** {@inheritDoc} */
    @Override
    public Utilizator adaugaUtilizator(String prenume, String nume) {
        Utilizator u = new Utilizator(prenume, nume);
        long maxId = 0;
        for (Utilizator existent : repoUtilizatori.findAll()) {
            if (existent.getId() > maxId) {
                maxId = existent.getId();
            }
        }
        u.setId(maxId + 1);
        repoUtilizatori.save(u);
        return u;
    }

    /** {@inheritDoc} */
    @Override
    public Utilizator stergeUtilizator(Long id) {
        if (repoUtilizatori.findOne(id) == null) {
            throw new EntityNotFoundException("Utilizatorul " + id + " nu exista.");
        }
        for (Prietenie p : repoPrietenii.findAll()) {
            if (p.getIdUtilizator1().equals(id) || p.getIdUtilizator2().equals(id)) {
                repoPrietenii.delete(p.getId());
            }
        }
        return repoUtilizatori.delete(id);
    }

    /** {@inheritDoc} */
    @Override
    public Utilizator gasesteUtilizator(Long id) {
        Utilizator u = repoUtilizatori.findOne(id);
        if (u == null) {
            throw new EntityNotFoundException("Utilizatorul " + id + " nu exista.");
        }
        return u;
    }

    /** {@inheritDoc} */
    @Override
    public List<Utilizator> getUtilizatori() {
        List<Utilizator> utilizatori = new ArrayList<Utilizator>();
        for (Utilizator u : repoUtilizatori.findAll()) {
            u.getPrieteni().clear();
            utilizatori.add(u);
        }
        for (Prietenie p : repoPrietenii.findAll()) {
            Utilizator a = repoUtilizatori.findOne(p.getIdUtilizator1());
            Utilizator b = repoUtilizatori.findOne(p.getIdUtilizator2());
            if (a != null && b != null) {
                a.getPrieteni().add(b);
                b.getPrieteni().add(a);
            }
        }
        return utilizatori;
    }
}
