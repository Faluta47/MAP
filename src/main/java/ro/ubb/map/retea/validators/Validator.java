package ro.ubb.map.retea.validators;

import ro.ubb.map.retea.exceptions.ValidationException;

import java.util.Objects;

/**
 * Validation strategy for entities of a given type (Strategy pattern).
 * It is a functional interface: a new rule can be written as a lambda and chained
 * with an existing validator through {@link #andThen(Validator)}.
 *
 * @param <T> the type of the validated entity
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates the entity.
     *
     * @param entity the entity to validate
     * @throws ValidationException if the entity is invalid (the message contains all the errors)
     */
    void validate(T entity);

    /**
     * Composes this validator with another one: the returned validator runs this validator first and,
     * only if it accepts the entity, runs {@code other} as well.
     *
     * @param other the validator applied after this one; cannot be null
     * @return a validator that applies both rules, in order
     * @throws NullPointerException if {@code other} is null
     */
    default Validator<T> andThen(Validator<T> other) {
        Objects.requireNonNull(other, "other");
        return entity -> {
            validate(entity);
            other.validate(entity);
        };
    }
}
