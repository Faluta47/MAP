package ro.ubb.map.retea.repository.file;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.UserRepository;
import ro.ubb.map.retea.repository.UsernameSearch;
import ro.ubb.map.retea.validators.Validator;

/**
 * Users repository stored in a text file. Line format: {@code id;prenume;nume}
 * or {@code id;prenume;nume;username} when the user has a username
 * (the friends list is not saved).
 */
public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> implements UserRepository {

    /**
     * Creates the repository; the file is read on first use.
     *
     * @param validator the users validator
     * @param fileName  the path of the file
     * @param warnings  the collector for the loading warnings
     */
    public UtilizatorFileRepository(Validator<Utilizator> validator, String fileName, LoadWarnings warnings) {
        super(validator, fileName, warnings);
    }

    /** {@inheritDoc} */
    @Override
    protected Utilizator extractEntity(String linie) {
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

    /** {@inheritDoc} */
    @Override
    protected String createEntityAsString(Utilizator u) {
        String linie = u.getId() + ";" + u.getPrenume() + ";" + u.getNume();
        return u.getUsername() == null ? linie : linie + ";" + u.getUsername();
    }

    /** {@inheritDoc} */
    @Override
    public Utilizator findByUsername(String username) {
        return UsernameSearch.find(findAll(), username);
    }
}
