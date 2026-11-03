package ro.ubb.map.retea.exceptions;

/**
 * Unchecked exception thrown when a requested entity (user, friendship) does not exist.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message the error message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
