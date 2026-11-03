package ro.ubb.map.retea.repository.factory;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ConfigException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.repository.db.PrietenieDbRepository;
import ro.ubb.map.retea.repository.db.UtilizatorDbRepository;
import ro.ubb.map.retea.repository.file.FileRepository;
import ro.ubb.map.retea.repository.file.PrietenieConverter;
import ro.ubb.map.retea.repository.file.UtilizatorConverter;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.Validator;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Factory that creates the repositories according to the {@code repository.type} property
 * (MEMORY, FILE or DATABASE). It is the only place in the application that instantiates concrete
 * repositories. There is no switch: each {@link RepositoryType} is mapped, in an {@link EnumMap}, to a
 * {@link Function} that builds the repository from its validator, and only the function of the chosen
 * type is run (so the properties of the other types are not even read).
 */
public class RepositoryFactory {
    private static final String TYPE_KEY = "repository.type";
    private static final String UTILIZATORI_FILE_KEY = "file.utilizatori";
    private static final String PRIETENII_FILE_KEY = "file.prietenii";
    private static final String DB_URL_KEY = "db.url";
    private static final String DB_USER_KEY = "db.user";
    private static final String DB_PASSWORD_KEY = "db.password";

    private final Properties properties;
    private final Map<RepositoryType, Function<Validator<Utilizator>, Repository<Long, Utilizator>>> utilizatorCreators =
            new EnumMap<>(RepositoryType.class);
    private final Map<RepositoryType, Function<Validator<Prietenie>, Repository<Tuple<Long, Long>, Prietenie>>>
            prietenieCreators = new EnumMap<>(RepositoryType.class);

    /**
     * Creates the factory.
     *
     * @param properties the application configuration
     * @param warnings   the collector given to the repositories that load data (FILE)
     */
    public RepositoryFactory(Properties properties, LoadWarnings warnings) {
        this.properties = properties;

        utilizatorCreators.put(RepositoryType.MEMORY, InMemoryRepository::new);
        utilizatorCreators.put(RepositoryType.FILE, validator -> new FileRepository<>(required(UTILIZATORI_FILE_KEY),
                validator, UtilizatorConverter::fromLine, UtilizatorConverter::toLine, warnings));
        utilizatorCreators.put(RepositoryType.DATABASE, validator -> new UtilizatorDbRepository(validator,
                required(DB_URL_KEY), required(DB_USER_KEY), present(DB_PASSWORD_KEY)));

        prietenieCreators.put(RepositoryType.MEMORY, InMemoryRepository::new);
        prietenieCreators.put(RepositoryType.FILE, validator -> new FileRepository<>(required(PRIETENII_FILE_KEY),
                validator, PrietenieConverter::fromLine, PrietenieConverter::toLine, warnings));
        prietenieCreators.put(RepositoryType.DATABASE, validator -> new PrietenieDbRepository(validator,
                required(DB_URL_KEY), required(DB_USER_KEY), present(DB_PASSWORD_KEY)));
    }

    /**
     * Creates the users repository.
     *
     * @param validator the users validator
     * @return a repository of the configured type
     * @throws ConfigException if a needed property is missing or the type is unknown
     */
    public Repository<Long, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        return create(utilizatorCreators, validator);
    }

    /**
     * Creates the friendships repository, with the same persistence type as the users repository.
     *
     * @param validator the friendships validator
     * @return a repository of the configured type
     * @throws ConfigException if a needed property is missing or the type is unknown
     */
    public Repository<Tuple<Long, Long>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        return create(prietenieCreators, validator);
    }

    /**
     * Runs the creator registered for the configured type.
     *
     * @param creators  the creators, by persistence type
     * @param validator the validator given to the created repository
     * @param <ID>      the type of the id
     * @param <E>       the type of the entity
     * @return the created repository
     * @throws ConfigException if the type is missing, unknown or has no creator, or a needed property is missing
     */
    private <ID, E extends Entity<ID>> Repository<ID, E> create(
            Map<RepositoryType, Function<Validator<E>, Repository<ID, E>>> creators, Validator<E> validator) {
        RepositoryType type = readType();
        return Optional.ofNullable(creators.get(type))
                .orElseThrow(() -> new ConfigException("Tip de repository nesuportat: " + type))
                .apply(validator);
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
