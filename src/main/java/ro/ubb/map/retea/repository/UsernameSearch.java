package ro.ubb.map.retea.repository;

import ro.ubb.map.retea.domain.Utilizator;

/**
 * Shared lookup of a user by username in a collection, used by the {@link UserRepository} implementations.
 */
public final class UsernameSearch {
    private UsernameSearch() {
    }

    /**
     * Finds the first user with the given username.
     *
     * @param utilizatori the users to search in
     * @param username    the username to look for; cannot be null
     * @return the user found, or null if there is none
     * @throws IllegalArgumentException if the username is null
     */
    public static Utilizator find(Iterable<Utilizator> utilizatori, String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username-ul nu poate fi null.");
        }
        for (Utilizator u : utilizatori) {
            if (username.equals(u.getUsername())) {
                return u;
            }
        }
        return null;
    }
}
