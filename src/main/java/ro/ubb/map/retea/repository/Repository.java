package ro.ubb.map.retea.repository;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ValidationException;

/**
 * CRUD contract for entities identified by an ID.
 *
 * @param <ID> the type of the id
 * @param <E>  the type of the entity
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Finds an entity by id.
     *
     * @param id the id to look for; cannot be null
     * @return the entity with the given id, or null if it does not exist
     * @throws IllegalArgumentException if the id is null
     * @throws RepositoryException      if the persistent storage cannot be read
     */
    E findOne(ID id);

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
     * @return null if the entity was saved, otherwise the existing entity (the id already exists)
     * @throws IllegalArgumentException if the entity is null
     * @throws ValidationException      if the entity is not valid
     * @throws RepositoryException      if the persistent storage cannot be read or written
     */
    E save(E entity);

    /**
     * Deletes the entity with the given id.
     *
     * @param id the id of the entity; cannot be null
     * @return the deleted entity, or null if it does not exist
     * @throws IllegalArgumentException if the id is null
     * @throws RepositoryException      if the persistent storage cannot be read or written
     */
    E delete(ID id);

    /**
     * Updates an existing entity (with the same id), after validating it.
     *
     * @param entity the new version of the entity; cannot be null
     * @return null if the entity was updated, otherwise the given entity (the id does not exist)
     * @throws IllegalArgumentException if the entity is null
     * @throws ValidationException      if the entity is not valid
     * @throws RepositoryException      if the persistent storage cannot be read or written
     */
    E update(E entity);
}
