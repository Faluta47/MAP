package ro.ubb.map.retea;

import org.junit.jupiter.api.Test;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.PrietenieValidator;
import ro.ubb.map.retea.validators.UtilizatorValidator;
import ro.ubb.map.retea.validators.Validator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        ValidationException e = assertThrows(ValidationException.class, () -> utilizatorValidator.validate(utilizator(null, "", " ")));
        String m = e.getMessage();
        assertTrue(m.contains("Id-ul"));
        assertTrue(m.contains("Numele"));
        assertTrue(m.contains("Prenumele"));
    }

    @Test
    void numeCuPunctSiVirgulaEsteInvalid() {
        ValidationException e = assertThrows(ValidationException.class, () -> utilizatorValidator.validate(utilizator(1L, "Io;n", "Pop")));
        assertTrue(e.getMessage().contains("Prenumele"));
    }

    @Test
    void prietenieValidaPasaValidarea() {
        prietenieValidator.validate(new Prietenie(1L, 2L));
    }

    @Test
    void prietenieCuSineEsteInvalida() {
        ValidationException e = assertThrows(ValidationException.class, () -> prietenieValidator.validate(new Prietenie(3L, 3L)));
        assertTrue(e.getMessage().contains("insusi"));
    }

    @Test
    void prietenieCuIdNullEsteInvalida() {
        ValidationException e = assertThrows(ValidationException.class, () -> prietenieValidator.validate(new Prietenie(null, 3L)));
        assertTrue(e.getMessage().contains("null"));
    }

    @Test
    void prietenieCuUtilizatorInexistentEsteInvalida() {
        InMemoryRepository<Long, Utilizator> repoU =
                new InMemoryRepository<Long, Utilizator>(utilizatorValidator);
        repoU.save(utilizator(1L, "Ion", "Pop"));
        PrietenieValidator cuExistenta = new PrietenieValidator(repoU);
        ValidationException e = assertThrows(ValidationException.class, () -> cuExistenta.validate(new Prietenie(1L, 2L)));
        assertTrue(e.getMessage().contains("2"));
        repoU.save(utilizator(2L, "Ana", "Pop"));
        cuExistenta.validate(new Prietenie(1L, 2L));
    }

    /** Extra rule written as a lambda, like in Main. */
    private final Validator<Utilizator> numeScurt = u -> {
        if (u.getNume().trim().length() > 5) {
            throw new ValidationException("Numele poate avea cel mult 5 caractere.");
        }
    };

    @Test
    void andThenAplicaRegulaLambdaPesteValidatorulExistent() {
        Validator<Utilizator> compus = utilizatorValidator.andThen(numeScurt);
        compus.validate(utilizator(1L, "Ion", "Pop"));
        ValidationException e = assertThrows(ValidationException.class,
                () -> compus.validate(utilizator(1L, "Ion", "Popescu")));
        assertTrue(e.getMessage().contains("cel mult 5"));
    }

    @Test
    void andThenPastreazaRegulileValidatoruluiExistent() {
        Validator<Utilizator> compus = utilizatorValidator.andThen(numeScurt);
        ValidationException e = assertThrows(ValidationException.class,
                () -> compus.validate(utilizator(1L, "", "Pop")));
        assertTrue(e.getMessage().contains("Prenumele"));
    }

    @Test
    void andThenRuleazaPrimaRegulaInainteDeAdoua() {
        List<String> ordine = new ArrayList<>();
        Validator<Utilizator> prima = u -> ordine.add("prima");
        Validator<Utilizator> adoua = u -> ordine.add("a doua");
        prima.andThen(adoua).validate(utilizator(1L, "Ion", "Pop"));
        assertEquals(List.of("prima", "a doua"), ordine);
    }

    @Test
    void andThenNuRuleazaAdouaRegulaCandPrimaRespinge() {
        List<String> ordine = new ArrayList<>();
        Validator<Utilizator> prima = u -> {
            throw new ValidationException("prima");
        };
        Validator<Utilizator> adoua = u -> ordine.add("a doua");
        assertThrows(ValidationException.class, () -> prima.andThen(adoua).validate(utilizator(1L, "Ion", "Pop")));
        assertTrue(ordine.isEmpty());
    }

    @Test
    void andThenCuNullAruncaNullPointerException() {
        assertThrows(NullPointerException.class, () -> utilizatorValidator.andThen(null));
    }

    @Test
    void validatorulComusFunctioneazaInRepository() {
        InMemoryRepository<Long, Utilizator> repo =
                new InMemoryRepository<>(utilizatorValidator.andThen(numeScurt));
        assertTrue(repo.save(utilizator(1L, "Ion", "Pop")).isEmpty());
        assertThrows(ValidationException.class, () -> repo.save(utilizator(2L, "Ana", "Georgescu")));
    }
}
