package ro.ubb.map.retea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.ServiceException;
import ro.ubb.map.retea.exceptions.ValidationException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

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
        for (int i = 1; i <= 7; i++) {
            utilizatorService.adaugaUtilizator("P" + i, "N" + i);
        }
    }

    @Test
    void idulSeGenereazaMaxPlusUnu() {
        utilizatorService.stergeUtilizator(3L);
        assertEquals(8L, utilizatorService.adaugaUtilizator("P", "N").getId().longValue());
    }

    @Test
    void utilizatorInvalidAruncaValidare() {
        try {
            utilizatorService.adaugaUtilizator("P", "");
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            // asteptat
        }
        try {
            utilizatorService.adaugaUtilizator(null, "N");
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            // asteptat
        }
    }

    @Test
    void prietenieDuplicataSiInversaRespinse() {
        prietenieService.adaugaPrietenie(1L, 2L);
        try {
            prietenieService.adaugaPrietenie(1L, 2L);
            fail("Se astepta ServiceException");
        } catch (ServiceException e) {
            // asteptat
        }
        try {
            prietenieService.adaugaPrietenie(2L, 1L);
            fail("Se astepta ServiceException");
        } catch (ServiceException e) {
            // asteptat
        }
        assertEquals(1, prietenieService.getPrietenii().size());
    }

    @Test
    void prietenieCuSineRespinsa() {
        try {
            prietenieService.adaugaPrietenie(1L, 1L);
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            // asteptat
        }
    }

    @Test
    void prietenieCuUtilizatorInexistentRespinsa() {
        try {
            prietenieService.adaugaPrietenie(1L, 99L);
            fail("Se astepta EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            // asteptat
        }
    }

    @Test
    void stergePrietenieInexistentaAruncaEroare() {
        try {
            prietenieService.stergePrietenie(1L, 2L);
            fail("Se astepta EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            // asteptat
        }
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
        assertNull(repoP.findOne(new Prietenie(1L, 2L).getId()));
        try {
            utilizatorService.stergeUtilizator(1L);
            fail("Se astepta EntityNotFoundException");
        } catch (EntityNotFoundException e) {
            // asteptat
        }
    }

    @Test
    void listaDePrieteniPopulata() {
        prietenieService.adaugaPrietenie(1L, 2L);
        for (Utilizator u : utilizatorService.getUtilizatori()) {
            if (u.getId() == 1L || u.getId() == 2L) {
                assertEquals(1, u.getPrieteni().size());
            } else {
                assertEquals(0, u.getPrieteni().size());
            }
        }
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
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate();
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
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate();
        assertEquals(3, c.getLungimeDrum());
        assertEquals(4, c.getMembri().size());
    }

    @Test
    void ceaMaiSociabilaLaEgalitateEPrima() {
        prietenieService.adaugaPrietenie(5L, 6L);
        prietenieService.adaugaPrietenie(1L, 2L);
        ComunitateSociabila c = reteaService.ceaMaiSociabilaComunitate();
        assertEquals(1, c.getLungimeDrum());
        assertEquals(1L, c.getMembri().get(0).getId().longValue());
    }

    @Test
    void faraPrieteniiLungimeZero() {
        assertEquals(0, reteaService.ceaMaiSociabilaComunitate().getLungimeDrum());
    }
}
