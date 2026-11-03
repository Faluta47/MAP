package ro.ubb.map.retea.repository.memory;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.validators.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Repository that keeps the entities in memory, in a Map (insertion order is preserved).
 *
 * @param <ID> the type of the id
 * @param <E>  the type of the entity
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    private Validator<E> validator;
    private Map<ID, E> entities = new LinkedHashMap<ID, E>();

    /**
     * Creates an empty repository.
     *
     * @param validator the validator applied on save and update
     */
    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
    }

    /** {@inheritDoc} */
    @Override
    public E findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null.");
        }
        return entities.get(id);
    }

    /**
     * {@inheritDoc}
     * Returns a copy, so the repository can be modified while the result is traversed.
     */
    @Override
    public Iterable<E> findAll() {
        return new ArrayList<E>(entities.values());
    }

    /** {@inheritDoc} */
    @Override
    public E save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null.");
        }
        validator.validate(entity);
        E existent = entities.get(entity.getId());
        if (existent != null) {
            return existent;
        }
        entities.put(entity.getId(), entity);
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public E delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null.");
        }
        return entities.remove(id);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ValidationException if the entity is not valid
     */
    @Override
    public E update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null.");
        }
        validator.validate(entity);
        if (entities.containsKey(entity.getId())) {
            entities.put(entity.getId(), entity);
            return null;
        }
        return entity;
    }
}
