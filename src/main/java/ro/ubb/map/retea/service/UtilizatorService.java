package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.ValidationException;

import java.util.List;

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
     * @return the user found
     * @throws EntityNotFoundException if the user does not exist
     */
    Utilizator gasesteUtilizator(Long id);

    /**
     * Returns the users, with the friends list filled in from the friendships.
     *
     * @return the list of users
     */
    List<Utilizator> getUtilizatori();
}
