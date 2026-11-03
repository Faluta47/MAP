package ro.ubb.map.retea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class InMemoryRepositoryTest {
    private InMemoryRepository<Long, Utilizator> repo;

    private Utilizator utilizator(Long id, String nume) {
        Utilizator u = new Utilizator("Prenume", nume);
        u.setId(id);
        return u;
    }

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepository<Long, Utilizator>(new UtilizatorValidator());
    }

    @Test
    void saveReturneazaNullCandSalveazaSiEntitateaCandIdulExista() {
        Utilizator u = utilizator(1L, "A");
        assertNull(repo.save(u));
        assertSame(u, repo.save(utilizator(1L, "B")));
        assertEquals("A", repo.findOne(1L).getNume());
    }

    @Test
    void saveNullAruncaIllegalArgument() {
        try {
            repo.save(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
    }

    @Test
    void saveInvalidAruncaValidationException() {
        try {
            repo.save(new Utilizator("", ""));
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            // asteptat
        }
    }

    @Test
    void findOneNullAruncaIllegalArgumentIarInexistentDaNull() {
        assertNull(repo.findOne(5L));
        try {
            repo.findOne(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
    }

    @Test
    void deleteReturneazaEntitateaStearsaSauNull() {
        Utilizator u = utilizator(1L, "A");
        repo.save(u);
        assertSame(u, repo.delete(1L));
        assertNull(repo.delete(1L));
        try {
            repo.delete(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
    }

    @Test
    void updateReturneazaNullCandActualizeazaSiEntitateaCandIdulLipseste() {
        repo.save(utilizator(1L, "A"));
        assertNull(repo.update(utilizator(1L, "Nou")));
        assertEquals("Nou", repo.findOne(1L).getNume());
        Utilizator inexistent = utilizator(2L, "X");
        assertSame(inexistent, repo.update(inexistent));
    }

    @Test
    void updateNullSauInvalidAruncaExceptii() {
        repo.save(utilizator(1L, "A"));
        try {
            repo.update(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
        try {
            repo.update(new Utilizator("P", ""));
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            // asteptat
        }
    }

    @Test
    void findAllReturneazaToateEntitatile() {
        repo.save(utilizator(1L, "A"));
        repo.save(utilizator(2L, "B"));
        int numar = 0;
        for (Utilizator u : repo.findAll()) {
            numar++;
        }
        assertEquals(2, numar);
    }

    @Test
    void prietenia12EsteAceeasiCu21() {
        InMemoryRepository<Tuple<Long, Long>, Prietenie> repoP =
                new InMemoryRepository<Tuple<Long, Long>, Prietenie>(new PrietenieValidator());
        Prietenie p = new Prietenie(1L, 2L);
        assertNull(repoP.save(p));
        assertSame(p, repoP.save(new Prietenie(2L, 1L)));
    }

    @Test
    void findByUsernameInMemoryGasesteSauIntoarceNull() {
        ro.ubb.map.retea.repository.memory.UtilizatorInMemoryRepository repo =
                new ro.ubb.map.retea.repository.memory.UtilizatorInMemoryRepository(
                        new ro.ubb.map.retea.validators.UtilizatorValidator());
        Utilizator u = new Utilizator("Ion", "Pop");
        u.setId(1L);
        u.setUsername("ionp");
        repo.save(u);
        assertEquals(1L, repo.findByUsername("ionp").getId().longValue());
        assertNull(repo.findByUsername("altcineva"));
    }
}
