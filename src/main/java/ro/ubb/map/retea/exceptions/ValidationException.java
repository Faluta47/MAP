package ro.ubb.map.retea.exceptions;

/**
 * Unchecked exception thrown when an entity or an input does not respect the validation rules.
 */
public class ValidationException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message the error message (all the problems found)
     */
    public ValidationException(String message) {
        super(message);
    }
}
