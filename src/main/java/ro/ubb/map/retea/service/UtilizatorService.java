package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Business logic for users: add, delete (cascading to friendships), find, list.
 */
public interface UtilizatorService {

    /**
     * Adds a user with an automatically generated id (existing maximum + 1).
     *
     * @param prenume the first name
     * @param nume    the last name
     * @return the added user
     * @throws ValidationException if the data is invalid
     */
    Utilizator adaugaUtilizator(String prenume, String nume);

    /**
     * Deletes a user and all of their friendships.
     *
     * @param id the id of the user
     * @return the deleted user
     * @throws EntityNotFoundException if the user does not exist
     */
    Utilizator stergeUtilizator(Long id);

    /**
     * Finds a user by id.
     *
     * @param id the id to look for
     * @return the user found, or an empty Optional if the user does not exist
     */
    Optional<Utilizator> gasesteUtilizator(Long id);

    /**
     * Returns the users, with the friends list filled in from the friendships.
     *
     * @return the list of users
     */
    List<Utilizator> getUtilizatori();

    /**
     * Searches the users that satisfy a criterion. The friends lists are filled in first, so criteria
     * based on friends (for example {@code CriteriiUtilizator.faraPrieteni()}) work.
     *
     * @param criteriu the criterion a user must satisfy; criteria can be combined with
     *                 {@code and}, {@code or}, {@code negate}; cannot be null
     * @return the matching users, in repository order; empty if there are none
     * @throws IllegalArgumentException if the criterion is null
     */
    List<Utilizator> cautaUtilizatori(Predicate<Utilizator> criteriu);
}
