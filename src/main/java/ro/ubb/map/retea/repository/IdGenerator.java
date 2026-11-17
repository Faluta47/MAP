package ro.ubb.map.retea.repository;

import ro.ubb.map.retea.domain.Entity;

import java.util.stream.Stream;

/**
 * Generates the id of an entity that is saved without one. It is used by the repositories that do not
 * have a storage able to generate ids by itself (MEMORY and FILE); the database generates its own ids.
 *
 * @param <ID> the type of the id
 * @param <E>  the type of the entity
 */
@FunctionalInterface
public interface IdGenerator<ID, E extends Entity<ID>> {

    /**
     * Computes the id for a new entity.
     *
     * @param existing the entities already stored
     * @return the new id
     */
    ID nextId(Stream<E> existing);

    /**
     * Generator of numeric ids: the biggest existing id plus one (1 when there is no entity).
     *
     * @param <E> the type of the entity
     * @return the generator
     */
    static <E extends Entity<Long>> IdGenerator<Long, E> maxPlusOne() {
        return existing -> existing.mapToLong(Entity::getId).max().orElse(0L) + 1;
    }
}
