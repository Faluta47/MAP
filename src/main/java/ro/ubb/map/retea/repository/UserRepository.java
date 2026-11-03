package ro.ubb.map.retea.repository;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.RepositoryException;

/**
 * Users repository, with lookup by username.
 */
public interface UserRepository extends Repository<Long, Utilizator> {

    /**
     * Finds a user by username (exact match).
     *
     * @param username the username to look for; cannot be null
     * @return the user with the given username, or null if it does not exist
     * @throws IllegalArgumentException if the username is null
     * @throws RepositoryException      if the persistent storage cannot be read
     */
    Utilizator findByUsername(String username);
}
