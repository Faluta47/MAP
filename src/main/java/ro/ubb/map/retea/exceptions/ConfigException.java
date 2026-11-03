package ro.ubb.map.retea.exceptions;

/**
 * Unchecked exception thrown when the application configuration is missing, unreadable or invalid.
 */
public class ConfigException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message the error message (which key, which value)
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a cause.
     *
     * @param message the error message
     * @param cause   the original cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
