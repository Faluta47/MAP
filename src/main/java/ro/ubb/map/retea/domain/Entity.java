package ro.ubb.map.retea.domain;

import java.util.Objects;

/**
 * Base class for the application entities, identified by an id.
 *
 * @param <ID> the type of the identifier
 */
public abstract class Entity<ID> {
    private ID id;

    /**
     * Returns the identifier of the entity.
     *
     * @return the id of the entity
     */
    public ID getId() {
        return id;
    }

    /**
     * Sets the identifier of the entity.
     *
     * @param id the new id
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * Two entities are equal if they have the same class and the same id.
     *
     * @param o the object to compare with
     * @return true if the objects are equal; false for null or for an object of another class
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    /**
     * Hash code computed from the id, consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
