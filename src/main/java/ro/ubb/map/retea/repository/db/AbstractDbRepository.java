package ro.ubb.map.retea.repository.db;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.validators.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Common part of the JDBC repositories: the connection settings, opening a connection and converting
 * {@link SQLException} to {@link RepositoryException}. No connection is kept in a field: every operation
 * opens its own connection in a try-with-resources block.
 *
 * @param <ID> the type of the id
 * @param <E>  the type of the entity
 */
abstract class AbstractDbRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    protected final Validator<E> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Stores the settings; it does not open any connection.
     *
     * @param validator the validator applied on save and update
     * @param url       the JDBC url (db.url)
     * @param user      the database user (db.user)
     * @param password  the database password (db.password)
     */
    protected AbstractDbRepository(Validator<E> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Opens a new connection; the caller closes it.
     *
     * @return a new connection
     * @throws SQLException if the connection cannot be opened
     */
    protected Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Wraps a database error; the message depends on the SQLState (see {@link SqlStateMessages}).
     *
     * @param e the database error
     * @return the exception to throw, having {@code e} as cause
     */
    protected static RepositoryException wrap(SQLException e) {
        return new RepositoryException(SqlStateMessages.toMessage(e.getSQLState(), e.getMessage()), e);
    }

    /**
     * Checks that an argument is not null.
     *
     * @param value   the argument
     * @param message the message of the exception
     * @throws IllegalArgumentException if the argument is null
     */
    protected static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
