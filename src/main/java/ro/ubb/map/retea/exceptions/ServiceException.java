package ro.ubb.map.retea.exceptions;

/**
 * Unchecked exception for business-logic errors (for example a duplicate friendship).
 */
public class ServiceException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message the error message
     */
    public ServiceException(String message) {
        super(message);
    }
}
