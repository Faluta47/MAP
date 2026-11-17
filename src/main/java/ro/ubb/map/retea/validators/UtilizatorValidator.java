package ro.ubb.map.retea.validators;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ValidationException;

/**
 * Validator for {@link Utilizator}: non-blank names and optional username, without line breaks.
 * The id may be null: it is assigned by the storage when the user is saved. The file separator ';' is
 * not checked here, it is a limit of the file format (see UtilizatorConverter).
 */
public class UtilizatorValidator implements Validator<Utilizator> {

    /**
     * Validates the user and gathers all the errors in a single message.
     *
     * @param u the user to validate
     * @throws ValidationException if any field is invalid
     */
    @Override
    public void validate(Utilizator u) {
        StringBuilder erori = new StringBuilder();
        verificaNume(u.getPrenume(), "Prenumele", erori);
        verificaNume(u.getNume(), "Numele", erori);
        if (u.getUsername() != null) {
            verificaNume(u.getUsername(), "Username-ul", erori);
        }
        if (erori.length() > 0) {
            throw new ValidationException(erori.toString().trim());
        }
    }

    /** Checks that a name is not blank and has no line break. */
    private void verificaNume(String valoare, String eticheta, StringBuilder erori) {
        if (valoare == null || valoare.trim().isEmpty()) {
            erori.append(eticheta).append(" nu poate fi vid. ");
        } else if (valoare.indexOf('\n') >= 0 || valoare.indexOf('\r') >= 0) {
            erori.append(eticheta).append(" nu poate contine sfarsit de linie. ");
        }
    }
}
