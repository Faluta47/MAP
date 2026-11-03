package ro.ubb.map.retea.repository.memory;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.validators.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Optional<E> findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null.");
        }
        return Optional.ofNullable(entities.get(id));
    }

    /**
     * {@inheritDoc}
     * Returns a copy, so the repository can be modified while the result is traversed.
     */
    @Override
    public Iterable<E> findAll() {
        return entities.values().stream().collect(Collectors.toCollection(ArrayList::new));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null.");
        }
        validator.validate(entity);
        // putIfAbsent returns the existing entity (and keeps it), or null when the entity was stored
        return Optional.ofNullable(entities.putIfAbsent(entity.getId(), entity));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null.");
        }
        return Optional.ofNullable(entities.remove(id));
    }

    /**
     * {@inheritDoc}
     *
     * @throws ValidationException if the entity is not valid
     */
    @Override
    public Optional<E> update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null.");
        }
        validator.validate(entity);
        // replace returns the previous entity, or null when the id is not stored
        return entities.replace(entity.getId(), entity) == null ? Optional.of(entity) : Optional.empty();
    }
}
