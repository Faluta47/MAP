package ro.ubb.map.retea.repository.file;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.RepositoryException;

/**
 * Conversion between a {@link Utilizator} and a line of the users file. Line format:
 * {@code id;prenume;nume} or {@code id;prenume;nume;username} when the user has a username
 * (the friends list is not saved). The methods are static so they can be passed to
 * {@link FileRepository} as method references.
 */
public final class UtilizatorConverter {
    private UtilizatorConverter() {
    }

    /**
     * Builds a user from a file line.
     *
     * @param linie the line that was read
     * @return the corresponding user
     * @throws RepositoryException if the line does not have 3 or 4 fields or the id is not a number
     *                             (the cause is the NumberFormatException in the latter case)
     */
    public static Utilizator fromLine(String linie) {
        String[] parts = linie.split(";", -1);
        if (parts.length != 3 && parts.length != 4) {
            throw new RepositoryException("se asteapta 3 campuri (id;prenume;nume), sau 4 cu username, gasite "
                    + parts.length);
        }
        try {
            Utilizator u = new Utilizator(parts[1].trim(), parts[2].trim());
            u.setId(Long.parseLong(parts[0].trim()));
            if (parts.length == 4) {
                u.setUsername(parts[3].trim());
            }
            return u;
        } catch (NumberFormatException e) {
            throw new RepositoryException("id nenumeric '" + parts[0].trim() + "'", e);
        }
    }

    /**
     * Serializes a user into a file line.
     *
     * @param u the user
     * @return the line to write
     */
    public static String toLine(Utilizator u) {
        String linie = u.getId() + ";" + u.getPrenume() + ";" + u.getNume();
        return u.getUsername() == null ? linie : linie + ";" + u.getUsername();
    }
}
