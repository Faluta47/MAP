package ro.ubb.map.retea.repository.file;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.exceptions.RepositoryException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Conversion between a {@link Prietenie} and a line of the friendships file. Line format:
 * {@code idUtilizator1;idUtilizator2;dataPrieteniei} (the date in ISO format, for example 2026-10-19T10:30).
 * The methods are static so they can be passed to {@link FileRepository} as method references.
 */
public final class PrietenieConverter {
    private PrietenieConverter() {
    }

    /**
     * Builds a friendship from a file line.
     *
     * @param linie the line that was read
     * @return the corresponding friendship
     * @throws RepositoryException if the line does not have 3 fields, an id is not a number or the date is
     *                             not valid (the cause is the NumberFormatException or DateTimeParseException)
     */
    public static Prietenie fromLine(String linie) {
        String[] parts = linie.split(";", -1);
        if (parts.length != 3) {
            throw new RepositoryException("se asteapta 3 campuri (id1;id2;data), gasite " + parts.length);
        }
        try {
            return new Prietenie(Long.parseLong(parts[0].trim()), Long.parseLong(parts[1].trim()),
                    LocalDateTime.parse(parts[2].trim()));
        } catch (NumberFormatException e) {
            throw new RepositoryException("id nenumeric", e);
        } catch (DateTimeParseException e) {
            throw new RepositoryException("data invalida '" + parts[2].trim() + "'", e);
        }
    }

    /**
     * Serializes a friendship into a file line.
     *
     * @param p the friendship
     * @return the line to write
     */
    public static String toLine(Prietenie p) {
        return p.getIdUtilizator1() + ";" + p.getIdUtilizator2() + ";"
                + p.getData().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
