package ro.ubb.map.retea.repository.file;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.validators.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Friendships repository stored in a text file. Line format:
 * {@code idUtilizator1;idUtilizator2;dataPrieteniei} (the date in ISO format, for example 2026-10-19T10:30).
 */
public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie> {

    /**
     * Creates the repository; the file is read on first use.
     *
     * @param validator the friendships validator
     * @param fileName  the path of the file
     * @param warnings  the collector for the loading warnings
     */
    public PrietenieFileRepository(Validator<Prietenie> validator, String fileName, LoadWarnings warnings) {
        super(validator, fileName, warnings);
    }

    /** {@inheritDoc} */
    @Override
    protected Prietenie extractEntity(String linie) {
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

    /** {@inheritDoc} */
    @Override
    protected String createEntityAsString(Prietenie p) {
        return p.getIdUtilizator1() + ";" + p.getIdUtilizator2() + ";"
                + p.getData().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
