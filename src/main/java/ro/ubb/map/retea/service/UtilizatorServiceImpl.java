package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.repository.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        long maxId = repoUtilizatori.stream().mapToLong(Entity::getId).max().orElse(0L);
        u.setId(maxId + 1);
        repoUtilizatori.save(u);
        return u;
    }

    /** {@inheritDoc} */
    @Override
    public Utilizator stergeUtilizator(Long id) {
        repoUtilizatori.findOne(id).orElseThrow(() -> neexistent(id));
        // findAll(Predicate) returns a new list, so deleting while going through it is safe
        repoPrietenii.findAll(p -> p.getIdUtilizator1().equals(id) || p.getIdUtilizator2().equals(id))
                .stream()
                .map(Entity::getId)
                .forEach(repoPrietenii::delete);
        return repoUtilizatori.delete(id).orElseThrow(() -> neexistent(id));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Utilizator> gasesteUtilizator(Long id) {
        return repoUtilizatori.findOne(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<Utilizator> getUtilizatori() {
        List<Utilizator> utilizatori = repoUtilizatori.stream().collect(Collectors.toList());
        Map<Long, Utilizator> dupaId = utilizatori.stream()
                .collect(Collectors.toMap(Entity::getId, Function.identity()));
        // the friends lists are rebuilt from the friendships (terminal operations that change the lists)
        utilizatori.forEach(u -> u.getPrieteni().clear());
        repoPrietenii.stream()
                .filter(p -> dupaId.containsKey(p.getIdUtilizator1()) && dupaId.containsKey(p.getIdUtilizator2()))
                .forEach(p -> leagaPrieteni(dupaId.get(p.getIdUtilizator1()), dupaId.get(p.getIdUtilizator2())));
        return utilizatori;
    }

    /** {@inheritDoc} */
    @Override
    public List<Utilizator> cautaUtilizatori(Predicate<Utilizator> criteriu) {
        getUtilizatori();
        return repoUtilizatori.findAll(criteriu);
    }

    private static void leagaPrieteni(Utilizator a, Utilizator b) {
        a.getPrieteni().add(b);
        b.getPrieteni().add(a);
    }

    private static EntityNotFoundException neexistent(Long id) {
        return new EntityNotFoundException("Utilizatorul " + id + " nu exista.");
    }
}
