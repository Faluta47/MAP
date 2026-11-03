package ro.ubb.map.retea;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.file.PrietenieFileRepository;
import ro.ubb.map.retea.repository.file.UtilizatorFileRepository;
import ro.ubb.map.retea.service.PrietenieService;
import ro.ubb.map.retea.service.PrietenieServiceImpl;
import ro.ubb.map.retea.service.ReteaService;
import ro.ubb.map.retea.service.ReteaServiceImpl;
import ro.ubb.map.retea.service.UtilizatorService;
import ro.ubb.map.retea.service.UtilizatorServiceImpl;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FileRepositoryTest {

    private UtilizatorFileRepository repoUtilizatori(Path fisier, LoadWarnings warnings) {
        return new UtilizatorFileRepository(new UtilizatorValidator(), fisier.toString(), warnings);
    }

    private PrietenieFileRepository repoPrietenii(Path fisier, UtilizatorFileRepository utilizatori,
                                                  LoadWarnings warnings) {
        return new PrietenieFileRepository(new PrietenieValidator(utilizatori), fisier.toString(), warnings);
    }

    private Utilizator utilizator(Long id, String prenume, String nume) {
        Utilizator u = new Utilizator(prenume, nume);
        u.setId(id);
        return u;
    }

    private void scrie(Path fisier, String continut) throws Exception {
        Files.write(fisier, continut.getBytes(StandardCharsets.UTF_8));
    }

    private int numara(Iterable<?> elemente) {
        int numar = 0;
        for (Object o : elemente) {
            numar++;
        }
        return numar;
    }

    // ---- username

    @Test
    void findByUsernameGasesteUtilizatorulSalvatSiReincarcat(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("u.txt");
        UtilizatorFileRepository repo = repoUtilizatori(fisier, new LoadWarnings());
        Utilizator u = utilizator(1L, "Ion", "Pop");
        u.setUsername("ionp");
        assertNull(repo.save(u));
        assertNull(repo.save(utilizator(2L, "Ana", "Ene")));
        assertEquals("1;Ion;Pop;ionp", Files.readAllLines(fisier).get(0));
        assertEquals("2;Ana;Ene", Files.readAllLines(fisier).get(1));

        UtilizatorFileRepository reincarcat = repoUtilizatori(fisier, new LoadWarnings());
        assertEquals(1L, reincarcat.findByUsername("ionp").getId().longValue());
        assertNull(reincarcat.findByUsername("inexistent"));
    }

    @Test
    void findByUsernameCuNullAruncaIllegalArgument(@TempDir Path dir) {
        try {
            repoUtilizatori(dir.resolve("u.txt"), new LoadWarnings()).findByUsername(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    // ---- fisier lipsa

    @Test
    void fisierLipsaDaRepositoryGolSiFisierulSeCreeazaLaSalvare(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("nou").resolve("utilizatori.txt");
        LoadWarnings warnings = new LoadWarnings();
        UtilizatorFileRepository repo = repoUtilizatori(fisier, warnings);
        assertEquals(0, numara(repo.findAll()));
        assertTrue(warnings.getAll().isEmpty());
        assertFalse(Files.exists(fisier));

        assertNull(repo.save(utilizator(1L, "Ion", "Pop")));
        assertTrue(Files.exists(fisier));
        assertEquals("1;Ion;Pop", Files.readAllLines(fisier).get(0));
    }

    @Test
    void constructorulNuCitesteFisierul(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("u.txt");
        scrie(fisier, "x;y\n");
        LoadWarnings warnings = new LoadWarnings();
        repoUtilizatori(fisier, warnings);
        assertTrue(warnings.getAll().isEmpty());
    }

    // ---- persistenta

    @Test
    void salveazaActualizeazaStergeSiReincarca(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("u.txt");
        UtilizatorFileRepository repo = repoUtilizatori(fisier, new LoadWarnings());
        assertNull(repo.save(utilizator(1L, "Ion", "Pop")));
        assertEquals("1;Ion;Pop", Files.readAllLines(fisier).get(0));

        assertNull(repo.update(utilizator(1L, "Ion", "Popa")));
        assertEquals("1;Ion;Popa", Files.readAllLines(fisier).get(0));

        UtilizatorFileRepository reincarcat = repoUtilizatori(fisier, new LoadWarnings());
        assertEquals("Popa", reincarcat.findOne(1L).getNume());
        assertTrue(reincarcat.findOne(1L).getPrieteni().isEmpty());

        reincarcat.delete(1L);
        assertTrue(Files.readAllLines(fisier).isEmpty());
        assertNull(repoUtilizatori(fisier, new LoadWarnings()).findOne(1L));
    }

    @Test
    void operatiileEsuateNuReScriuFisierul(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("u.txt");
        scrie(fisier, "1;Ion;Pop\n");
        UtilizatorFileRepository repo = repoUtilizatori(fisier, new LoadWarnings());
        long modificat = Files.getLastModifiedTime(fisier).toMillis();
        assertEquals(1L, repo.save(utilizator(1L, "Alt", "Nume")).getId().longValue());
        assertNull(repo.delete(5L));
        assertEquals(modificat, Files.getLastModifiedTime(fisier).toMillis());
        assertEquals("1;Ion;Pop", Files.readAllLines(fisier).get(0));
    }

    @Test
    void prieteniileSePastreazaCuDataSiNormalizate(@TempDir Path dir) throws Exception {
        UtilizatorFileRepository utilizatori = repoUtilizatori(dir.resolve("u.txt"), new LoadWarnings());
        utilizatori.save(utilizator(2L, "A", "B"));
        utilizatori.save(utilizator(5L, "C", "D"));
        Path fisier = dir.resolve("p.txt");
        PrietenieFileRepository repo = repoPrietenii(fisier, utilizatori, new LoadWarnings());
        LocalDateTime data = LocalDateTime.of(2026, 10, 19, 10, 30, 0);
        assertNull(repo.save(new Prietenie(5L, 2L, data)));
        assertTrue(Files.readAllLines(fisier).get(0).startsWith("2;5;2026-10-19T10:30"));

        PrietenieFileRepository reincarcat = repoPrietenii(fisier, utilizatori, new LoadWarnings());
        assertEquals(data, reincarcat.findOne(new Prietenie(2L, 5L).getId()).getData());
    }

    @Test
    void diacriticeleSeSalveazaSiSeCitescInUtf8(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("u.txt");
        repoUtilizatori(fisier, new LoadWarnings()).save(utilizator(1L, "Ștefan", "Țăranu"));
        assertEquals("1;Ștefan;Țăranu", Files.readAllLines(fisier, StandardCharsets.UTF_8).get(0));
        assertEquals("Țăranu", repoUtilizatori(fisier, new LoadWarnings()).findOne(1L).getNume());
    }

    // ---- linii corupte

    @Test
    void liniileGoaleSeIgnoraFaraAvertisment(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("u.txt");
        scrie(fisier, "1;A;B\n\n   \n2;C;D\n");
        LoadWarnings warnings = new LoadWarnings();
        assertEquals(2, numara(repoUtilizatori(fisier, warnings).findAll()));
        assertTrue(warnings.getAll().isEmpty());
    }

    @Test
    void liniiCorupteSuntRaportateSiSarite(@TempDir Path dir) throws Exception {
        Path fisier = dir.resolve("u.txt");
        scrie(fisier, "1;Ion;Pop\n"
                + "2;Ana\n"
                + "x;Maria;Ionescu\n"
                + "4; ;Stan\n"
                + "1;Dublura;Id\n"
                + "6;Vlad;Radu\n");
        LoadWarnings warnings = new LoadWarnings();
        UtilizatorFileRepository repo = repoUtilizatori(fisier, warnings);
        assertEquals(2, numara(repo.findAll()));
        assertEquals("Pop", repo.findOne(1L).getNume());
        assertEquals("Radu", repo.findOne(6L).getNume());

        List<String> avertismente = warnings.getAll();
        assertEquals(4, avertismente.size());
        assertTrue(avertismente.get(0).contains("u.txt, linia 2"));
        assertTrue(avertismente.get(0).contains("3 campuri"));
        assertTrue(avertismente.get(1).contains("linia 3"));
        assertTrue(avertismente.get(1).contains("nenumeric"));
        assertTrue(avertismente.get(2).contains("linia 4"));
        assertTrue(avertismente.get(2).contains("Prenumele"));
        assertTrue(avertismente.get(3).contains("linia 5"));
        assertTrue(avertismente.get(3).contains("duplicat"));
    }

    @Test
    void prieteniiCorupteSuntRaportateSiSarite(@TempDir Path dir) throws Exception {
        UtilizatorFileRepository utilizatori = repoUtilizatori(dir.resolve("u.txt"), new LoadWarnings());
        utilizatori.save(utilizator(1L, "A", "A"));
        utilizatori.save(utilizator(2L, "B", "B"));
        utilizatori.save(utilizator(3L, "C", "C"));
        Path fisier = dir.resolve("p.txt");
        scrie(fisier, "1;2;2026-09-01T10:00\n"
                + "1;2\n"
                + "1;x;2026-09-01T10:00\n"
                + "2;3;ieri\n"
                + "3;3;2026-09-01T10:00\n"
                + "2;99;2026-09-01T10:00\n"
                + "2;3;2026-09-02T10:00\n");
        LoadWarnings warnings = new LoadWarnings();
        PrietenieFileRepository repo = repoPrietenii(fisier, utilizatori, warnings);
        assertEquals(2, numara(repo.findAll()));

        List<String> avertismente = warnings.getAll();
        assertEquals(5, avertismente.size());
        assertTrue(avertismente.get(0).contains("p.txt, linia 2"));
        assertTrue(avertismente.get(1).contains("linia 3"));
        assertTrue(avertismente.get(2).contains("linia 4"));
        assertTrue(avertismente.get(2).contains("data invalida"));
        assertTrue(avertismente.get(3).contains("linia 5"));
        assertTrue(avertismente.get(3).contains("insusi"));
        assertTrue(avertismente.get(4).contains("linia 6"));
        assertTrue(avertismente.get(4).contains("99"));
    }

    // ---- consistenta intre cele doua fisiere

    @Test
    void stergereaUnuiUtilizatorStergePrieteniileDinFisier(@TempDir Path dir) throws Exception {
        Path fisierU = dir.resolve("u.txt");
        Path fisierP = dir.resolve("p.txt");
        scrie(fisierU, "1;A;A\n2;B;B\n3;C;C\n");
        scrie(fisierP, "1;2;2026-09-01T10:00\n1;3;2026-09-02T10:00\n2;3;2026-09-03T10:00\n");
        LoadWarnings warnings = new LoadWarnings();
        UtilizatorFileRepository repoU = repoUtilizatori(fisierU, warnings);
        PrietenieFileRepository repoP = repoPrietenii(fisierP, repoU, warnings);
        UtilizatorService utilizatorService = new UtilizatorServiceImpl(repoU, repoP);

        utilizatorService.stergeUtilizator(1L);

        assertEquals(2, Files.readAllLines(fisierU).size());
        List<String> prietenii = Files.readAllLines(fisierP);
        assertEquals(1, prietenii.size());
        assertTrue(prietenii.get(0).startsWith("2;3;"));
        assertTrue(warnings.getAll().isEmpty());
    }

    @Test
    void prietenieCuUtilizatorInexistentNuSeSalveaza(@TempDir Path dir) throws Exception {
        Path fisierP = dir.resolve("p.txt");
        UtilizatorFileRepository repoU = repoUtilizatori(dir.resolve("u.txt"), new LoadWarnings());
        repoU.save(utilizator(1L, "A", "A"));
        PrietenieFileRepository repoP = repoPrietenii(fisierP, repoU, new LoadWarnings());
        PrietenieService prietenieService = new PrietenieServiceImpl(repoU, repoP);
        try {
            prietenieService.adaugaPrietenie(1L, 7L);
            fail("Se astepta EntityNotFoundException");
        } catch (ro.ubb.map.retea.exceptions.EntityNotFoundException e) {
            // asteptat
        }
        try {
            repoP.save(new Prietenie(1L, 7L));
            fail("Se astepta ValidationException");
        } catch (ro.ubb.map.retea.exceptions.ValidationException e) {
            // asteptat
        }
        assertFalse(Files.exists(fisierP));
    }

    @Test
    void serviciulRaporteazaAvertismenteleDeLaIncarcare(@TempDir Path dir) throws Exception {
        Path fisierU = dir.resolve("u.txt");
        Path fisierP = dir.resolve("p.txt");
        scrie(fisierU, "1;A;A\n2;B;B\nrau\n");
        scrie(fisierP, "1;2;2026-09-01T10:00\n1;9;2026-09-01T10:00\n");
        LoadWarnings warnings = new LoadWarnings();
        UtilizatorFileRepository repoU = repoUtilizatori(fisierU, warnings);
        PrietenieFileRepository repoP = repoPrietenii(fisierP, repoU, warnings);
        ReteaService reteaService = new ReteaServiceImpl(repoU, repoP, warnings);

        List<String> avertismente = reteaService.getAvertismenteIncarcare();
        assertEquals(2, avertismente.size());
        assertTrue(avertismente.get(0).contains("u.txt, linia 3"));
        assertTrue(avertismente.get(1).contains("p.txt, linia 2"));
        assertEquals(1, reteaService.numarComunitati());
    }

    // ---- contractul cu null

    @Test
    void contractulCuNullAlRepositoryuluiDeFisier(@TempDir Path dir) {
        UtilizatorFileRepository repo = repoUtilizatori(dir.resolve("u.txt"), new LoadWarnings());
        try {
            repo.findOne(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
        try {
            repo.save(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
        try {
            repo.delete(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
        try {
            repo.update(null);
            fail("Se astepta IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // asteptat
        }
    }

    @Test
    void fisierulNelizibilAruncaRepositoryException(@TempDir Path dir) {
        // a directory in place of the file: it exists but cannot be read as a file
        try {
            repoUtilizatori(dir, new LoadWarnings()).findAll();
            fail("Se astepta RepositoryException");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() != null);
        }
    }
}
