package ro.ubb.map.retea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.repository.CriteriiUtilizator;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CriteriiUtilizatorTest {
    private InMemoryRepository<Long, Utilizator> repo;

    private Utilizator utilizator(Long id, String prenume, String nume) {
        Utilizator u = new Utilizator(prenume, nume);
        u.setId(id);
        return u;
    }

    private List<Long> ids(Predicate<Utilizator> criteriu) {
        return repo.findAll(criteriu).stream().map(Utilizator::getId).collect(Collectors.toList());
    }

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepository<>(new UtilizatorValidator());
        Utilizator ion = utilizator(1L, "Ion", "Popescu");
        Utilizator ana = utilizator(2L, "Ana", "Pop");
        Utilizator maria = utilizator(3L, "Maria", "Ionescu");
        Utilizator dan = utilizator(4L, "Dan", "Stan");
        ion.getPrieteni().add(ana);
        ana.getPrieteni().add(ion);
        ana.getPrieteni().add(maria);
        maria.getPrieteni().add(ana);
        Stream.of(ion, ana, maria, dan).forEach(repo::save);
    }

    @Test
    void numeIncepeCuIgnoraMajusculele() {
        assertEquals(List.of(1L, 2L), ids(CriteriiUtilizator.numeIncepeCu("pop")));
    }

    @Test
    void prenumeContine() {
        assertEquals(List.of(1L, 3L), ids(CriteriiUtilizator.prenumeContine("io").or(CriteriiUtilizator.prenumeContine("ar"))));
        assertEquals(List.of(2L, 3L, 4L), ids(CriteriiUtilizator.prenumeContine("a")));
    }

    @Test
    void faraPrieteniSiMinimPrieteni() {
        assertEquals(List.of(4L), ids(CriteriiUtilizator.faraPrieteni()));
        assertEquals(List.of(2L), ids(CriteriiUtilizator.areMinimPrieteni(2)));
    }

    @Test
    void andIntersecteaza() {
        assertEquals(List.of(1L), ids(CriteriiUtilizator.numeIncepeCu("pop").and(CriteriiUtilizator.prenumeContine("io"))));
    }

    @Test
    void orReuneste() {
        assertEquals(List.of(1L, 2L, 4L),
                ids(CriteriiUtilizator.numeIncepeCu("pop").or(CriteriiUtilizator.numeIncepeCu("st"))));
    }

    @Test
    void negateSiPredicateNotInverseazaCriteriul() {
        assertEquals(List.of(1L, 2L, 3L), ids(CriteriiUtilizator.faraPrieteni().negate()));
        assertEquals(List.of(1L, 2L, 3L), ids(Predicate.not(CriteriiUtilizator.faraPrieteni())));
    }

    @Test
    void criteriuCombinatComplex() {
        // (nume incepe cu "ion" sau "pop") si nu fara prieteni
        Predicate<Utilizator> criteriu = CriteriiUtilizator.numeIncepeCu("ion")
                .or(CriteriiUtilizator.numeIncepeCu("pop"))
                .and(Predicate.not(CriteriiUtilizator.faraPrieteni()));
        assertEquals(List.of(1L, 2L, 3L), ids(criteriu));
    }

    @Test
    void maiMultePrefixeSeCombinaCuOr() {
        assertEquals(List.of(3L, 4L), ids(CriteriiUtilizator.numeIncepeCuUnulDin("ionescu, st , ")));
        assertEquals(List.of(), ids(CriteriiUtilizator.numeIncepeCuUnulDin(" , ")));
    }

    @Test
    void usernameSiArgumenteNull() {
        Utilizator u = utilizator(9L, "X", "Y");
        u.setUsername("xy");
        repo.save(u);
        assertEquals(List.of(9L), ids(CriteriiUtilizator.areUsername("xy")));
        assertThrows(IllegalArgumentException.class, () -> CriteriiUtilizator.areUsername(null));
        assertThrows(NullPointerException.class, () -> CriteriiUtilizator.numeIncepeCuUnulDin(null));
    }
}
