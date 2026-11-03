package ro.ubb.map.retea.repository.memory;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.repository.UserRepository;
import ro.ubb.map.retea.repository.UsernameSearch;
import ro.ubb.map.retea.validators.Validator;

/**
 * In-memory users repository, with lookup by username.
 */
public class UtilizatorInMemoryRepository extends InMemoryRepository<Long, Utilizator> implements UserRepository {

    /**
     * Creates an empty repository.
     *
     * @param validator the validator applied on save and update
     */
    public UtilizatorInMemoryRepository(Validator<Utilizator> validator) {
        super(validator);
    }

    /** {@inheritDoc} */
    @Override
    public Utilizator findByUsername(String username) {
        return UsernameSearch.find(findAll(), username);
    }
}
