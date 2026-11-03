package ro.ubb.map.retea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.CriteriiUtilizator;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void saveReturneazaEmptyCandSalveazaSiEntitateaCandIdulExista() {
        Utilizator u = utilizator(1L, "A");
        assertTrue(repo.save(u).isEmpty());
        assertSame(u, repo.save(utilizator(1L, "B")).orElseThrow());
        assertEquals("A", repo.findOne(1L).orElseThrow().getNume());
    }

    @Test
    void saveNullAruncaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void saveInvalidAruncaValidationException() {
        assertThrows(ValidationException.class, () -> repo.save(new Utilizator("", "")));
    }

    @Test
    void findOneNullAruncaIllegalArgumentIarInexistentDaEmpty() {
        assertFalse(repo.findOne(5L).isPresent());
        assertThrows(IllegalArgumentException.class, () -> repo.findOne(null));
    }

    @Test
    void deleteReturneazaEntitateaStearsaSauEmpty() {
        Utilizator u = utilizator(1L, "A");
        repo.save(u);
        assertSame(u, repo.delete(1L).orElseThrow());
        assertTrue(repo.delete(1L).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repo.delete(null));
    }

    @Test
    void updateReturneazaEmptyCandActualizeazaSiEntitateaCandIdulLipseste() {
        repo.save(utilizator(1L, "A"));
        assertTrue(repo.update(utilizator(1L, "Nou")).isEmpty());
        assertEquals("Nou", repo.findOne(1L).orElseThrow().getNume());
        Utilizator inexistent = utilizator(2L, "X");
        assertSame(inexistent, repo.update(inexistent).orElseThrow());
    }

    @Test
    void updateNullSauInvalidAruncaExceptii() {
        repo.save(utilizator(1L, "A"));
        assertThrows(IllegalArgumentException.class, () -> repo.update(null));
        assertThrows(ValidationException.class, () -> repo.update(new Utilizator("P", "")));
    }

    @Test
    void findAllReturneazaToateEntitatile() {
        repo.save(utilizator(1L, "A"));
        repo.save(utilizator(2L, "B"));
        assertEquals(2, repo.stream().count());
    }

    @Test
    void prietenia12EsteAceeasiCu21() {
        InMemoryRepository<Tuple<Long, Long>, Prietenie> repoP =
                new InMemoryRepository<Tuple<Long, Long>, Prietenie>(new PrietenieValidator());
        Prietenie p = new Prietenie(1L, 2L);
        assertTrue(repoP.save(p).isEmpty());
        assertSame(p, repoP.save(new Prietenie(2L, 1L)).orElseThrow());
    }

    @Test
    void findAllCuPredicatFiltreazaSiPastreazaOrdinea() {
        repo.save(utilizator(1L, "Ana"));
        repo.save(utilizator(2L, "Bob"));
        repo.save(utilizator(3L, "Alina"));
        List<Utilizator> rezultat = repo.findAll(CriteriiUtilizator.numeIncepeCu("a"));
        assertEquals(List.of(1L, 3L), rezultat.stream().map(Utilizator::getId).collect(Collectors.toList()));
        assertTrue(repo.findAll(CriteriiUtilizator.numeIncepeCu("z")).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repo.findAll((Predicate<Utilizator>) null));
    }

    @Test
    void usernameSeCautaCuCriteriuInLocDeMetodaDedicata() {
        Utilizator u = new Utilizator("Ion", "Pop");
        u.setId(1L);
        u.setUsername("ionp");
        repo.save(u);
        Optional<Utilizator> gasit = repo.findAll(CriteriiUtilizator.areUsername("ionp")).stream().findFirst();
        assertEquals(1L, gasit.orElseThrow().getId().longValue());
        assertTrue(repo.findAll(CriteriiUtilizator.areUsername("altcineva")).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> CriteriiUtilizator.areUsername(null));
    }
}
