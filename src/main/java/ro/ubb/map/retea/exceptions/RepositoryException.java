package ro.ubb.map.retea.exceptions;

/**
 * Unchecked exception thrown on persistence errors. It wraps IOException, NumberFormatException and
 * DateTimeParseException (exception chaining) and also reports malformed file lines.
 */
public class RepositoryException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message the error message
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a cause.
     *
     * @param message the error message
     * @param cause   the original cause
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
