package ro.ubb.map.retea;

import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ValidatoriTest {
    private final UtilizatorValidator utilizatorValidator = new UtilizatorValidator();
    private final PrietenieValidator prietenieValidator = new PrietenieValidator();

    private Utilizator utilizator(Long id, String prenume, String nume) {
        Utilizator u = new Utilizator(prenume, nume);
        u.setId(id);
        return u;
    }

    @Test
    void utilizatorValidPasaValidarea() {
        utilizatorValidator.validate(utilizator(1L, "Ion", "Pop"));
    }

    @Test
    void utilizatorInvalidAdunaToateErorile() {
        try {
            utilizatorValidator.validate(utilizator(null, "", " "));
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            String m = e.getMessage();
            assertTrue(m.contains("Id-ul"));
            assertTrue(m.contains("Numele"));
            assertTrue(m.contains("Prenumele"));
        }
    }

    @Test
    void numeCuPunctSiVirgulaEsteInvalid() {
        try {
            utilizatorValidator.validate(utilizator(1L, "Io;n", "Pop"));
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Prenumele"));
        }
    }

    @Test
    void prietenieValidaPasaValidarea() {
        prietenieValidator.validate(new Prietenie(1L, 2L));
    }

    @Test
    void prietenieCuSineEsteInvalida() {
        try {
            prietenieValidator.validate(new Prietenie(3L, 3L));
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("insusi"));
        }
    }

    @Test
    void prietenieCuIdNullEsteInvalida() {
        try {
            prietenieValidator.validate(new Prietenie(null, 3L));
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("null"));
        }
    }

    @Test
    void prietenieCuUtilizatorInexistentEsteInvalida() {
        InMemoryRepository<Long, Utilizator> repoU =
                new InMemoryRepository<Long, Utilizator>(utilizatorValidator);
        repoU.save(utilizator(1L, "Ion", "Pop"));
        PrietenieValidator cuExistenta = new PrietenieValidator(repoU);
        try {
            cuExistenta.validate(new Prietenie(1L, 2L));
            fail("Se astepta ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("2"));
        }
        repoU.save(utilizator(2L, "Ana", "Pop"));
        cuExistenta.validate(new Prietenie(1L, 2L));
    }
}
