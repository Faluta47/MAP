package ro.ubb.map.retea.validators;

import ro.ubb.map.retea.exceptions.ValidationException;

/**
 * Validation strategy for entities of a given type (Strategy pattern).
 *
 * @param <T> the type of the validated entity
 */
public interface Validator<T> {

    /**
     * Validates the entity.
     *
     * @param entity the entity to validate
     * @throws ValidationException if the entity is invalid (the message contains all the errors)
     */
    void validate(T entity);
}
