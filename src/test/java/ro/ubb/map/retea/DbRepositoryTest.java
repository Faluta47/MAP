package ro.ubb.map.retea;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.repository.db.PrietenieDbRepository;
import ro.ubb.map.retea.repository.db.UtilizatorDbRepository;
import ro.ubb.map.retea.service.PrietenieService;
import ro.ubb.map.retea.service.PrietenieServiceImpl;
import ro.ubb.map.retea.service.UtilizatorService;
import ro.ubb.map.retea.service.UtilizatorServiceImpl;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests on a real PostgreSQL. They run only when TEST_DB_URL, TEST_DB_USER and
 * TEST_DB_PASSWORD are set (otherwise they are skipped) and work in their own temporary schema,
 * which is dropped at the end, so the data of the application is never touched.
 */
class DbRepositoryTest {
    private static final String NUME_DEMO = "'); DROP TABLE users; --";
    private static String schema;
    private static String url;
    private static String user;
    private static String password;

    private Repository<Long, Utilizator> repoU;
    private Repository<Tuple<Long, Long>, Prietenie> repoP;

    @BeforeAll
    static void creeazaSchema() throws SQLException {
        String baza = System.getenv("TEST_DB_URL");
        user = System.getenv("TEST_DB_USER");
        password = System.getenv("TEST_DB_PASSWORD");
        Assumptions.assumeTrue(baza != null && user != null && password != null,
                "TEST_DB_URL / TEST_DB_USER / TEST_DB_PASSWORD nu sunt setate: testele de integrare sunt sarite");
        schema = "lab5_test_" + UUID.randomUUID().toString().replace("-", "");
        url = baza + (baza.contains("?") ? "&" : "?") + "currentSchema=" + schema;
        try (Connection c = DriverManager.getConnection(baza, user, password); Statement st = c.createStatement()) {
            st.execute("CREATE SCHEMA " + schema);
        }
    }

    @AfterAll
    static void stergeSchema() throws SQLException {
        if (schema == null) {
            return;
        }
        try (Connection c = DriverManager.getConnection(url, user, password); Statement st = c.createStatement()) {
            st.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    @BeforeEach
    void reface() throws SQLException {
        try (Connection c = DriverManager.getConnection(url, user, password); Statement st = c.createStatement()) {
            st.execute("DROP TABLE IF EXISTS friendships");
            st.execute("DROP TABLE IF EXISTS users");
            st.execute("CREATE TABLE users (id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + " first_name VARCHAR NOT NULL, last_name VARCHAR NOT NULL)");
            st.execute("CREATE TABLE friendships (id1 BIGINT, id2 BIGINT, friends_from TIMESTAMP NOT NULL,"
                    + " PRIMARY KEY (id1, id2), CHECK (id1 < id2),"
                    + " FOREIGN KEY (id1) REFERENCES users(id) ON DELETE CASCADE,"
                    + " FOREIGN KEY (id2) REFERENCES users(id) ON DELETE CASCADE)");
        }
        repoU = new UtilizatorDbRepository(new UtilizatorValidator(), url, user, password);
        repoP = new PrietenieDbRepository(new PrietenieValidator(repoU), url, user, password);
    }

    private Utilizator salveaza(String prenume, String nume) {
        Utilizator u = new Utilizator(prenume, nume);
        assertFalse(repoU.save(u).isPresent());
        return u;
    }

    private long numaraUtilizatori() {
        return repoU.stream().count();
    }

    @Test
    void saveDaIdGeneratDeBazaSiIgnoraIdulDeLaApelant() {
        Utilizator a = salveaza("Ion", "Pop");
        Utilizator b = new Utilizator("Ana", "Ene");
        b.setId(999L);
        repoU.save(b);
        assertEquals(1L, a.getId().longValue());
        assertEquals(2L, b.getId().longValue());
        assertEquals("Ana", repoU.findOne(2L).map(Utilizator::getPrenume).orElse("?"));
        assertFalse(repoU.findOne(999L).isPresent());
    }

    @Test
    void findOneFindAllSiUpdate() {
        Utilizator a = salveaza("Ion", "Pop");
        salveaza("Ana", "Ene");
        assertEquals(2, numaraUtilizatori());
        assertEquals("Pop", repoU.findOne(a.getId()).map(Utilizator::getNume).orElse("?"));
        assertEquals(java.util.List.of(1L, 2L),
                repoU.stream().map(Utilizator::getId).collect(Collectors.toList()));
        Utilizator modificat = new Utilizator("Ion", "Popescu");
        modificat.setId(a.getId());
        assertFalse(repoU.update(modificat).isPresent());
        assertEquals("Popescu", repoU.findOne(a.getId()).map(Utilizator::getNume).orElse("?"));
    }

    @Test
    void updatePeIdInexistentIntoarceEntitatea() {
        Utilizator fantoma = new Utilizator("X", "Y");
        fantoma.setId(42L);
        assertTrue(repoU.update(fantoma).isPresent());
        assertTrue(repoU.update(new Utilizator("X", "Y")).isPresent());
    }

    @Test
    void deleteIntoarceEntitateaStearsaSauEmpty() {
        Utilizator a = salveaza("Ion", "Pop");
        assertEquals("Ion", repoU.delete(a.getId()).map(Utilizator::getPrenume).orElse("?"));
        assertFalse(repoU.delete(a.getId()).isPresent());
        assertEquals(0, numaraUtilizatori());
    }

    @Test
    void contractulCuNullAruncaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> repoU.findOne(null));
        assertThrows(IllegalArgumentException.class, () -> repoU.save(null));
        assertThrows(IllegalArgumentException.class, () -> repoU.delete(null));
        assertThrows(IllegalArgumentException.class, () -> repoU.update(null));
        assertThrows(IllegalArgumentException.class, () -> repoP.findOne(null));
        assertThrows(IllegalArgumentException.class, () -> repoP.save(null));
    }

    @Test
    void utilizatorInvalidEsteRespinsInainteDeBaza() {
        assertThrows(ValidationException.class, () -> repoU.save(new Utilizator("", "Pop")));
        assertEquals(0, numaraUtilizatori());
    }

    @Test
    void numeleCuDropTableSeSalveazaCaTextSiTabelaRamane() throws SQLException {
        Utilizator u = salveaza("Ana", NUME_DEMO);
        assertEquals(NUME_DEMO, repoU.findOne(u.getId()).map(Utilizator::getNume).orElse("?"));
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM users")) {
            rs.next();
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void prietenieSalvataSiCititaCuDataCorecta() {
        Utilizator a = salveaza("A", "A");
        Utilizator b = salveaza("B", "B");
        LocalDateTime data = LocalDateTime.of(2026, 9, 1, 10, 0);
        assertFalse(repoP.save(new Prietenie(b.getId(), a.getId(), data)).isPresent());
        Prietenie citita = repoP.findOne(new Tuple<>(a.getId(), b.getId())).orElseThrow(AssertionError::new);
        assertEquals(data, citita.getData());
        assertEquals(a.getId(), citita.getIdUtilizator1());
        assertEquals(1, repoP.stream().count());
    }

    @Test
    void prietenieDuplicataIntoarceEntitatea() {
        Utilizator a = salveaza("A", "A");
        Utilizator b = salveaza("B", "B");
        assertFalse(repoP.save(new Prietenie(a.getId(), b.getId())).isPresent());
        assertTrue(repoP.save(new Prietenie(b.getId(), a.getId())).isPresent());
        assertEquals(1, repoP.stream().count());
    }

    @Test
    void updateSiDeletePePrietenie() {
        Utilizator a = salveaza("A", "A");
        Utilizator b = salveaza("B", "B");
        repoP.save(new Prietenie(a.getId(), b.getId(), LocalDateTime.of(2026, 1, 1, 0, 0)));
        LocalDateTime noua = LocalDateTime.of(2026, 2, 2, 3, 4);
        assertFalse(repoP.update(new Prietenie(a.getId(), b.getId(), noua)).isPresent());
        assertEquals(noua, repoP.findOne(new Tuple<>(a.getId(), b.getId())).map(Prietenie::getData).orElse(null));
        Utilizator c = salveaza("C", "C");
        // the friendship (a, c) does not exist: update reports it by returning the entity
        assertTrue(repoP.update(new Prietenie(a.getId(), c.getId(), noua)).isPresent());
        assertTrue(repoP.delete(new Tuple<>(a.getId(), b.getId())).isPresent());
        assertFalse(repoP.delete(new Tuple<>(a.getId(), b.getId())).isPresent());
    }

    @Test
    void prietenieCuUtilizatorInexistentEsteRespinsaDeValidator() {
        Utilizator a = salveaza("A", "A");
        assertThrows(ValidationException.class, () -> repoP.save(new Prietenie(a.getId(), 99L)));
    }

    @Test
    void cheiaStrainaIncalcataDaRepositoryExceptionCuMesajSpecific() {
        // without the users check of the validator, the database itself refuses the friendship
        Repository<Tuple<Long, Long>, Prietenie> faraValidare =
                new PrietenieDbRepository(p -> { }, url, user, password);
        RepositoryException e = assertThrows(RepositoryException.class,
                () -> faraValidare.save(new Prietenie(1L, 2L)));
        assertTrue(e.getMessage().contains("Utilizator inexistent"), e.getMessage());
        assertTrue(e.getCause() instanceof SQLException);
    }

    @Test
    void checkIncalcatDaMesajDespreId1MaiMic() {
        Utilizator a = salveaza("A", "A");
        Utilizator b = salveaza("B", "B");
        Repository<Tuple<Long, Long>, Prietenie> faraValidare =
                new PrietenieDbRepository(p -> { }, url, user, password);
        Prietenie inversata = new Prietenie(a.getId(), b.getId());
        inversata.setId(new Tuple<>(b.getId(), a.getId()));
        RepositoryException e = assertThrows(RepositoryException.class, () -> faraValidare.save(inversata));
        assertTrue(e.getMessage().contains("id1 < id2"), e.getMessage());
    }

    @Test
    void serviciulStergeUtilizatorulSiPrieteniileLui() {
        UtilizatorService us = new UtilizatorServiceImpl(repoU, repoP);
        PrietenieService ps = new PrietenieServiceImpl(repoU, repoP);
        Utilizator a = us.adaugaUtilizator("A", "A");
        Utilizator b = us.adaugaUtilizator("B", "B");
        ps.adaugaPrietenie(a.getId(), b.getId());
        us.stergeUtilizator(a.getId());
        assertEquals(0, repoP.stream().count());
        assertEquals(1, numaraUtilizatori());
    }

    @Test
    void cascadaDinBazaStergePrieteniileCandUtilizatorulEStersDirect() {
        Utilizator a = salveaza("A", "A");
        Utilizator b = salveaza("B", "B");
        repoP.save(new Prietenie(a.getId(), b.getId()));
        repoU.delete(a.getId());
        assertEquals(0, repoP.stream().count());
    }

    @Test
    void serverInaccesibilDaRepositoryExceptionCuMesajulDeIndisponibilitate() {
        Repository<Long, Utilizator> repo = new UtilizatorDbRepository(new UtilizatorValidator(),
                "jdbc:postgresql://localhost:1/socialnetwork", user, password);
        RepositoryException e = assertThrows(RepositoryException.class, repo::findAll);
        assertTrue(e.getMessage().contains("Baza de date nu este disponibila"), e.getMessage());
    }

    @Test
    void parolaGresitaDaMesajDeAutentificareFaraParola() {
        Repository<Long, Utilizator> repo = new UtilizatorDbRepository(new UtilizatorValidator(), url, user,
                password + "_gresita");
        RepositoryException e = assertThrows(RepositoryException.class, repo::findAll);
        assertTrue(e.getMessage().contains("user sau parola gresita"), e.getMessage());
        assertFalse(e.getMessage().contains(password));
    }
}
