package ro.ubb.map.retea.repository;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * CRUD contract for entities identified by an ID. Operations that may have no result return
 * {@link Optional} instead of null.
 *
 * @param <ID> the type of the id
 * @param <E>  the type of the entity
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Finds an entity by id.
     *
     * @param id the id to look for; cannot be null
     * @return the entity with the given id, or an empty Optional if it does not exist
     * @throws IllegalArgumentException if the id is null
     * @throws RepositoryException      if the persistent storage cannot be read
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all the entities.
     *
     * @return all the entities
     * @throws RepositoryException if the persistent storage cannot be read
     */
    Iterable<E> findAll();

    /**
     * Saves an entity after validating it.
     *
     * @param entity the entity to save; cannot be null
     * @return an empty Optional if the entity was saved, otherwise the existing entity (the id already exists)
     * @throws IllegalArgumentException if the entity is null
     * @throws ValidationException      if the entity is not valid
     * @throws RepositoryException      if the persistent storage cannot be read or written
     */
    Optional<E> save(E entity);

    /**
     * Deletes the entity with the given id.
     *
     * @param id the id of the entity; cannot be null
     * @return the deleted entity, or an empty Optional if it does not exist
     * @throws IllegalArgumentException if the id is null
     * @throws RepositoryException      if the persistent storage cannot be read or written
     */
    Optional<E> delete(ID id);

    /**
     * Updates an existing entity (with the same id), after validating it.
     *
     * @param entity the new version of the entity; cannot be null
     * @return an empty Optional if the entity was updated, otherwise the given entity (the id does not exist)
     * @throws IllegalArgumentException if the entity is null
     * @throws ValidationException      if the entity is not valid
     * @throws RepositoryException      if the persistent storage cannot be read or written
     */
    Optional<E> update(E entity);

    /**
     * Returns the entities as a new sequential stream. This is the only place where the
     * {@link Iterable} returned by {@link #findAll()} is turned into a stream.
     * A stream can be consumed only once, so every call builds a fresh one.
     *
     * @return a stream over a snapshot of all the entities
     * @throws RepositoryException if the persistent storage cannot be read
     */
    default Stream<E> stream() {
        return StreamSupport.stream(findAll().spliterator(), false);
    }

    /**
     * Returns the entities that satisfy a criterion, in the order of {@link #findAll()}.
     * Criteria are built outside the repository (see {@link CriteriiUtilizator}) and can be combined
     * with {@link Predicate#and(Predicate)}, {@link Predicate#or(Predicate)} and {@link Predicate#negate()}.
     *
     * @param filter the criterion an entity must satisfy; cannot be null
     * @return a new list with the matching entities, empty if there are none
     * @throws IllegalArgumentException if the filter is null
     * @throws RepositoryException      if the persistent storage cannot be read
     */
    default List<E> findAll(Predicate<E> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Criteriul nu poate fi null.");
        }
        return stream().filter(filter).collect(Collectors.toList());
    }
}
