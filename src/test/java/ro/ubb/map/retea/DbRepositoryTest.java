package ro.ubb.map.retea;

import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.repository.db.PrietenieDbRepository;
import ro.ubb.map.retea.repository.db.UtilizatorDbRepository;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DbRepositoryTest {

    private void cereNeimplementat(String operatie, String mesaj) {
        assertTrue(mesaj.contains(operatie), mesaj);
        assertTrue(mesaj.contains("Lab 5"), mesaj);
        assertFalse(mesaj.contains("Lab 4"), mesaj);
    }

    @Test
    void utilizatorDbAruncaUnsupportedOperationLaFiecareMetoda() {
        Repository<Long, Utilizator> repo =
                new UtilizatorDbRepository(new UtilizatorValidator(), "url", "user", "parola");
        Utilizator u = new Utilizator("Ion", "Pop");
        u.setId(1L);
        try {
            repo.findOne(1L);
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("findOne", e.getMessage());
        }
        try {
            repo.findAll();
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("findAll", e.getMessage());
        }
        try {
            repo.save(u);
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("save", e.getMessage());
        }
        try {
            repo.delete(1L);
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("delete", e.getMessage());
        }
        try {
            repo.update(u);
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("update", e.getMessage());
        }
    }

    @Test
    void prietenieDbAruncaUnsupportedOperationLaFiecareMetoda() {
        Repository<Tuple<Long, Long>, Prietenie> repo =
                new PrietenieDbRepository(new PrietenieValidator(), "url", "user", "parola");
        Prietenie p = new Prietenie(1L, 2L);
        try {
            repo.findOne(p.getId());
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("findOne", e.getMessage());
        }
        try {
            repo.findAll();
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("findAll", e.getMessage());
        }
        try {
            repo.save(p);
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("save", e.getMessage());
        }
        try {
            repo.delete(p.getId());
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("delete", e.getMessage());
        }
        try {
            repo.update(p);
            fail("Se astepta UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            cereNeimplementat("update", e.getMessage());
        }
    }
}
