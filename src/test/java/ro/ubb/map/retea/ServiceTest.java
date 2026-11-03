package ro.ubb.map.retea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.ServiceException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.CriteriiUtilizator;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.service.ComunitateSociabila;
import ro.ubb.map.retea.service.PrietenieService;
import ro.ubb.map.retea.service.PrietenieServiceImpl;
import ro.ubb.map.retea.service.ReteaService;
import ro.ubb.map.retea.service.ReteaServiceImpl;
import ro.ubb.map.retea.service.UtilizatorService;
import ro.ubb.map.retea.service.UtilizatorServiceImpl;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceTest {
    private InMemoryRepository<Tuple<Long, Long>, Prietenie> repoP;
    private UtilizatorService utilizatorService;
    private PrietenieService prietenieService;
    private ReteaService reteaService;

    @BeforeEach
    void setUp() {
        InMemoryRepository<Long, Utilizator> repoU =
                new InMemoryRepository<Long, Utilizator>(new UtilizatorValidator());
        repoP = new InMemoryRepository<Tuple<Long, Long>, Prietenie>(new PrietenieValidator(repoU));
        utilizatorService = new UtilizatorServiceImpl(repoU, repoP);
        prietenieService = new PrietenieServiceImpl(repoU, repoP);
        reteaService = new ReteaServiceImpl(repoU, repoP, new LoadWarnings());
        IntStream.rangeClosed(1, 7).forEach(i -> utilizatorService.adaugaUtilizator("P" + i, "N" + i));
    }

    @Test
    void idulSeGenereazaMaxPlusUnu() {
        utilizatorService.stergeUtilizator(3L);
        assertEquals(8L, utilizatorService.adaugaUtilizator("P", "N").getId().longValue());
    }

    @Test
    void utilizatorInvalidAruncaValidare() {
        assertThrows(ValidationException.class, () -> utilizatorService.adaugaUtilizator("P", ""));
        assertThrows(ValidationException.class, () -> utilizatorService.adaugaUtilizator(null, "N"));
    }

    @Test
    void prietenieDuplicataSiInversaRespinse() {
        prietenieService.adaugaPrietenie(1L, 2L);
        assertThrows(ServiceException.class, () -> prietenieService.adaugaPrietenie(1L, 2L));
        assertThrows(ServiceException.class, () -> prietenieService.adaugaPrietenie(2L, 1L));
        assertEquals(1, prietenieService.getPrietenii().size());
    }

    @Test
    void prietenieCuSineRespinsa() {
        assertThrows(ValidationException.class, () -> prietenieService.adaugaPrietenie(1L, 1L));
    }

    @Test
    void prietenieCuUtilizatorInexistentRespinsa() {
        assertThrows(EntityNotFoundException.class, () -> prietenieService.adaugaPrietenie(1L, 99L));
    }

    @Test
    void stergePrietenieInexistentaAruncaEroare() {
        assertThrows(EntityNotFoundException.class, () -> prietenieService.stergePrietenie(1L, 2L));
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.stergePrietenie(2L, 1L);
        assertEquals(0, prietenieService.getPrietenii().size());
    }

    @Test
    void stergereUtilizatorStergePrieteniile() {
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(3L, 1L);
        prietenieService.adaugaPrietenie(2L, 3L);
        utilizatorService.stergeUtilizator(1L);
        assertEquals(1, prietenieService.getPrietenii().size());
        assertTrue(repoP.findOne(new Prietenie(1L, 2L).getId()).isEmpty());
        assertThrows(EntityNotFoundException.class, () -> utilizatorService.stergeUtilizator(1L));
    }

    @Test
    void listaDePrieteniPopulata() {
        prietenieService.adaugaPrietenie(1L, 2L);
        utilizatorService.getUtilizatori().forEach(u -> {
            if (u.getId() == 1L || u.getId() == 2L) {
                assertEquals(1, u.getPrieteni().size());
            } else {
                assertEquals(0, u.getPrieteni().size());
            }
        });
    }

    @Test
    void numarComunitati() {
        assertEquals(7, reteaService.numarComunitati());
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(2L, 3L);
        prietenieService.adaugaPrietenie(5L, 6L);
        assertEquals(4, reteaService.numarComunitati());
    }

    @Test
    void stergereaNoduluiMijlociuCreesteNumarulComunitatilor() {
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(2L, 3L);
        assertEquals(5, reteaService.numarComunitati());
        utilizatorService.stergeUtilizator(2L);
        assertEquals(6, reteaService.numarComunitati());
    }

    @Test
    void ceaMaiSociabilaComunitate() {
        // {1,2,3,4}: ciclu 1-2-3-4-1, drum de 3 muchii; {5,6}: 1 muchie
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(2L, 3L);
        prietenieService.adaugaPrietenie(3L, 4L);
        prietenieService.adaugaPrietenie(1L, 4L);
        prietenieService.adaugaPrietenie(5L, 6L);
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate().orElseThrow();
        assertEquals(4, c.getMembri().size());
        assertEquals(3, c.getLungimeDrum());
        assertEquals(4, c.getDrum().size());
    }

    @Test
    void lantulBateComunitateaDensa() {
        // lant 1-2-3-4 (3 muchii) vs. triunghi 5-6-7 (2 muchii)
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(2L, 3L);
        prietenieService.adaugaPrietenie(3L, 4L);
        prietenieService.adaugaPrietenie(5L, 6L);
        prietenieService.adaugaPrietenie(6L, 7L);
        prietenieService.adaugaPrietenie(5L, 7L);
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate().orElseThrow();
        assertEquals(3, c.getLungimeDrum());
        assertEquals(4, c.getMembri().size());
    }

    @Test
    void ceaMaiSociabilaLaEgalitateEPrima() {
        prietenieService.adaugaPrietenie(5L, 6L);
        prietenieService.adaugaPrietenie(1L, 2L);
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate().orElseThrow();
        assertEquals(1, c.getLungimeDrum());
        assertEquals(1L, c.getMembri().get(0).getId().longValue());
    }

    @Test
    void faraPrieteniiLungimeZero() {
        assertEquals(0, reteaService.ceaMaiSociabilaComunitate().orElseThrow().getLungimeDrum());
    }

    private List<Long> ids(List<Utilizator> utilizatori) {
        return utilizatori.stream().map(Utilizator::getId).collect(Collectors.toList());
    }

    @Test
    void gasesteUtilizatorIntoarceOptional() {
        assertEquals("P3", utilizatorService.gasesteUtilizator(3L).orElseThrow().getPrenume());
        assertFalse(utilizatorService.gasesteUtilizator(99L).isPresent());
        assertThrows(IllegalArgumentException.class, () -> utilizatorService.gasesteUtilizator(null));
    }

    @Test
    void cautaUtilizatoriCuCriteriiCombinate() {
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(2L, 3L);
        // nume N1 sau N2, dar nu cele fara prieteni
        Predicate<Utilizator> criteriu = CriteriiUtilizator.numeIncepeCu("n1")
                .or(CriteriiUtilizator.numeIncepeCu("n2"))
                .and(Predicate.not(CriteriiUtilizator.faraPrieteni()));
        assertEquals(List.of(1L, 2L), ids(utilizatorService.cautaUtilizatori(criteriu)));
        assertEquals(List.of(2L), ids(utilizatorService.cautaUtilizatori(CriteriiUtilizator.areMinimPrieteni(2))));
        assertEquals(List.of(4L, 5L, 6L, 7L),
                ids(utilizatorService.cautaUtilizatori(CriteriiUtilizator.faraPrieteni())));
        assertThrows(IllegalArgumentException.class, () -> utilizatorService.cautaUtilizatori(null));
    }

    @Test
    void comunitatileSuntInOrdineaPrimuluiMembruSiInPreordineDfs() {
        // vecinii lui 1 in ordinea prieteniilor: 3, 2; 3 are vecin pe 4
        prietenieService.adaugaPrietenie(1L, 3L);
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(3L, 4L);
        prietenieService.adaugaPrietenie(6L, 7L);
        List<List<Long>> comunitati = reteaService.comunitati().stream().map(this::ids).collect(Collectors.toList());
        assertEquals(List.of(List.of(1L, 3L, 4L, 2L), List.of(5L), List.of(6L, 7L)), comunitati);
    }

    @Test
    void ceaMaiSociabilaFaraUtilizatoriEEmpty() {
        InMemoryRepository<Long, Utilizator> repoU = new InMemoryRepository<>(new UtilizatorValidator());
        ReteaService gol = new ReteaServiceImpl(repoU,
                new InMemoryRepository<Tuple<Long, Long>, Prietenie>(new PrietenieValidator(repoU)),
                new LoadWarnings());
        assertTrue(gol.ceaMaiSociabilaComunitate().isEmpty());
        assertEquals(0, gol.numarComunitati());
    }

    @Test
    void ceaMaiSociabilaLaEgalitateEPrimaGasita() {
        // doua comunitati cu drum de 2 muchii: castiga cea cu primul membru mai mic
        prietenieService.adaugaPrietenie(4L, 5L);
        prietenieService.adaugaPrietenie(5L, 6L);
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(2L, 3L);
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate().orElseThrow();
        assertEquals(List.of(1L, 2L, 3L), ids(c.getMembri()));
        assertEquals(List.of(1L, 2L, 3L), ids(c.getDrum()));
    }

    @Test
    void drumulLaEgalitateEPrimulGasitInCautare() {
        // ciclu 1-2-3-4-1: din 1, vecinii in ordinea prieteniilor sunt 2 apoi 4; primul drum de 3 muchii e 1-2-3-4
        prietenieService.adaugaPrietenie(1L, 2L);
        prietenieService.adaugaPrietenie(2L, 3L);
        prietenieService.adaugaPrietenie(3L, 4L);
        prietenieService.adaugaPrietenie(1L, 4L);
        assertEquals(List.of(1L, 2L, 3L, 4L), ids(reteaService.ceaMaiSociabilaComunitate().orElseThrow().getDrum()));
    }
}
