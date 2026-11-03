package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.exceptions.EntityNotFoundException;
import ro.ubb.map.retea.exceptions.ServiceException;
import ro.ubb.map.retea.exceptions.ValidationException;

import java.util.List;

/**
 * Business logic for friendships: add and delete, with the related checks.
 */
public interface PrietenieService {

    /**
     * Adds a friendship (1-2 is the same as 2-1).
     *
     * @param id1 the first user
     * @param id2 the second user
     * @return the added friendship
     * @throws ValidationException     if an id is null or id1 equals id2
     * @throws EntityNotFoundException if a user does not exist
     * @throws ServiceException        if the friendship already exists
     */
    Prietenie adaugaPrietenie(Long id1, Long id2);

    /**
     * Deletes a friendship.
     *
     * @param id1 the first user
     * @param id2 the second user
     * @return the deleted friendship
     * @throws ValidationException     if an id is null
     * @throws EntityNotFoundException if the friendship does not exist
     */
    Prietenie stergePrietenie(Long id1, Long id2);

    /**
     * Returns all the friendships.
     *
     * @return the list of friendships
     */
    List<Prietenie> getPrietenii();
}
