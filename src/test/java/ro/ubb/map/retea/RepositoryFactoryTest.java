package ro.ubb.map.retea;

import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ConfigException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.repository.db.PrietenieDbRepository;
import ro.ubb.map.retea.repository.db.UtilizatorDbRepository;
import ro.ubb.map.retea.repository.factory.RepositoryFactory;
import ro.ubb.map.retea.repository.file.PrietenieFileRepository;
import ro.ubb.map.retea.repository.file.UtilizatorFileRepository;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.repository.memory.UtilizatorInMemoryRepository;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class RepositoryFactoryTest {

    private RepositoryFactory factory(Properties p) {
        return new RepositoryFactory(p, new LoadWarnings());
    }

    private Properties props(String tip) {
        Properties p = new Properties();
        p.setProperty("repository.type", tip);
        p.setProperty("file.utilizatori", "nu-exista/u.txt");
        p.setProperty("file.prietenii", "nu-exista/p.txt");
        p.setProperty("db.url", "jdbc:postgresql://localhost/retea");
        p.setProperty("db.user", "retea");
        p.setProperty("db.password", "");
        return p;
    }

    @Test
    void memoryCreeazaInMemoryRepositoryPentruAmbele() {
        RepositoryFactory f = factory(props("MEMORY"));
        Repository<Long, Utilizator> u = f.createUtilizatorRepository(new UtilizatorValidator());
        Repository<Tuple<Long, Long>, Prietenie> p = f.createPrietenieRepository(new PrietenieValidator());
        assertEquals(UtilizatorInMemoryRepository.class, u.getClass());
        assertEquals(InMemoryRepository.class, p.getClass());
    }

    @Test
    void fileCreeazaFileRepositoryPentruAmbele() {
        RepositoryFactory f = factory(props("FILE"));
        assertEquals(UtilizatorFileRepository.class,
                f.createUtilizatorRepository(new UtilizatorValidator()).getClass());
        assertEquals(PrietenieFileRepository.class,
                f.createPrietenieRepository(new PrietenieValidator()).getClass());
    }

    @Test
    void databaseCreeazaDbRepositoryPentruAmbele() {
        RepositoryFactory f = factory(props("DATABASE"));
        assertEquals(UtilizatorDbRepository.class,
                f.createUtilizatorRepository(new UtilizatorValidator()).getClass());
        assertEquals(PrietenieDbRepository.class,
                f.createPrietenieRepository(new PrietenieValidator()).getClass());
    }

    @Test
    void tipulNuDepindeDeMajusculeSauMinuscule() {
        assertEquals(UtilizatorInMemoryRepository.class,
                factory(props("memory")).createUtilizatorRepository(new UtilizatorValidator()).getClass());
    }

    @Test
    void proprietateaTypeLipsaDaMesajClar() {
        try {
            factory(new Properties()).createPrietenieRepository(new PrietenieValidator());
            fail("Se astepta ConfigException");
        } catch (ConfigException e) {
            assertTrue(e.getMessage().contains("repository.type"));
        }
    }

    @Test
    void tipNecunoscutDaMesajCuValoarea() {
        try {
            factory(props("SQL")).createUtilizatorRepository(new UtilizatorValidator());
            fail("Se astepta ConfigException");
        } catch (ConfigException e) {
            assertTrue(e.getMessage().contains("repository.type"));
            assertTrue(e.getMessage().contains("SQL"));
        }
    }

    @Test
    void caleaFisieruluiLipsaDaMesajClar() {
        Properties p = props("FILE");
        p.remove("file.utilizatori");
        try {
            factory(p).createUtilizatorRepository(new UtilizatorValidator());
            fail("Se astepta ConfigException");
        } catch (ConfigException e) {
            assertTrue(e.getMessage().contains("file.utilizatori"));
        }
    }

    @Test
    void urlulBazeiDeDateLipsaDaMesajClar() {
        Properties p = props("DATABASE");
        p.remove("db.url");
        try {
            factory(p).createPrietenieRepository(new PrietenieValidator());
            fail("Se astepta ConfigException");
        } catch (ConfigException e) {
            assertTrue(e.getMessage().contains("db.url"));
        }
    }
}
