package ro.ubb.map.retea.validators;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ValidationException;

/**
 * Validator for {@link Utilizator}: non-null id, non-blank names and optional username that do not contain the file separator.
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
        if (u.getId() == null) {
            erori.append("Id-ul nu poate fi null. ");
        }
        verificaNume(u.getPrenume(), "Prenumele", erori);
        verificaNume(u.getNume(), "Numele", erori);
        if (u.getUsername() != null) {
            verificaNume(u.getUsername(), "Username-ul", erori);
        }
        if (erori.length() > 0) {
            throw new ValidationException(erori.toString().trim());
        }
    }

    /** Checks that a name is not blank and cannot break the line format of the file. */
    private void verificaNume(String valoare, String eticheta, StringBuilder erori) {
        if (valoare == null || valoare.trim().isEmpty()) {
            erori.append(eticheta).append(" nu poate fi vid. ");
        } else if (valoare.indexOf(';') >= 0 || valoare.indexOf('\n') >= 0 || valoare.indexOf('\r') >= 0) {
            erori.append(eticheta).append(" nu poate contine ';' sau sfarsit de linie. ");
        }
    }
}
