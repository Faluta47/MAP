package ro.ubb.map.retea.repository.factory;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ConfigException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.repository.UserRepository;
import ro.ubb.map.retea.repository.db.PrietenieDbRepository;
import ro.ubb.map.retea.repository.db.UtilizatorDbRepository;
import ro.ubb.map.retea.repository.file.PrietenieFileRepository;
import ro.ubb.map.retea.repository.file.UtilizatorFileRepository;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.repository.memory.UtilizatorInMemoryRepository;
import ro.ubb.map.retea.validators.Validator;

import java.util.Locale;
import java.util.Properties;

/**
 * Simple Factory that creates the repositories according to the {@code repository.type} property
 * (MEMORY, FILE or DATABASE). It is the only place in the application that instantiates concrete
 * repositories.
 */
public class RepositoryFactory {
    private static final String TYPE_KEY = "repository.type";
    private static final String UTILIZATORI_FILE_KEY = "file.utilizatori";
    private static final String PRIETENII_FILE_KEY = "file.prietenii";
    private static final String DB_URL_KEY = "db.url";
    private static final String DB_USER_KEY = "db.user";
    private static final String DB_PASSWORD_KEY = "db.password";

    private Properties properties;
    private LoadWarnings warnings;

    /**
     * Creates the factory.
     *
     * @param properties the application configuration
     * @param warnings   the collector given to the repositories that load data (FILE)
     */
    public RepositoryFactory(Properties properties, LoadWarnings warnings) {
        this.properties = properties;
        this.warnings = warnings;
    }

    /**
     * Creates the users repository.
     *
     * @param validator the users validator
     * @return a repository of the configured type
     * @throws ConfigException if a needed property is missing or the type is unknown
     */
    public UserRepository createUtilizatorRepository(Validator<Utilizator> validator) {
        RepositoryType type = readType();
        switch (type) {
            case MEMORY:
                return new UtilizatorInMemoryRepository(validator);
            case FILE:
                return new UtilizatorFileRepository(validator, required(UTILIZATORI_FILE_KEY), warnings);
            case DATABASE:
                return new UtilizatorDbRepository(validator, required(DB_URL_KEY), required(DB_USER_KEY),
                        present(DB_PASSWORD_KEY));
            default:
                throw new ConfigException("Tip de repository nesuportat: " + type);
        }
    }

    /**
     * Creates the friendships repository, with the same persistence type as the users repository.
     *
     * @param validator the friendships validator
     * @return a repository of the configured type
     * @throws ConfigException if a needed property is missing or the type is unknown
     */
    public Repository<Tuple<Long, Long>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        RepositoryType type = readType();
        switch (type) {
            case MEMORY:
                return new InMemoryRepository<Tuple<Long, Long>, Prietenie>(validator);
            case FILE:
                return new PrietenieFileRepository(validator, required(PRIETENII_FILE_KEY), warnings);
            case DATABASE:
                return new PrietenieDbRepository(validator, required(DB_URL_KEY), required(DB_USER_KEY),
                        present(DB_PASSWORD_KEY));
            default:
                throw new ConfigException("Tip de repository nesuportat: " + type);
        }
    }

    private RepositoryType readType() {
        String value = required(TYPE_KEY);
        try {
            return RepositoryType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ConfigException("Valoare necunoscuta pentru " + TYPE_KEY + ": '" + value
                    + "' (valori permise: MEMORY, FILE, DATABASE).", e);
        }
    }

    private String required(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new ConfigException("Proprietatea '" + key + "' lipseste din configurare.");
        }
        return value.trim();
    }

    /** Like {@link #required(String)}, but the value may be empty (a database password can be empty). */
    private String present(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new ConfigException("Proprietatea '" + key + "' lipseste din configurare.");
        }
        return value.trim();
    }
}
