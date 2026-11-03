package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Utilizator;

import java.util.List;
import java.util.Optional;

/**
 * Analysis of the network: communities, the most sociable community and the loading report.
 */
public interface ReteaService {

    /**
     * Counts the communities (connected components of the friendship graph).
     *
     * @return the number of communities; a user without friends is a community of its own
     */
    int numarComunitati();

    /**
     * Determines the communities (connected components), in the order of their first member.
     *
     * @return the list of communities, each one being the list of its members
     */
    List<List<Utilizator>> comunitati();

    /**
     * Finds the community that contains the longest elementary path (no repeated nodes) between two users.
     * On a tie, the first one found is kept.
     *
     * @return the result, or an empty Optional if there are no users
     */
    Optional<ComunitateSociabila> ceaMaiSociabilaComunitate();

    /**
     * Returns the warnings produced while loading the persistent data (corrupt or inconsistent lines).
     * Calling it makes sure the data is loaded.
     *
     * @return the warnings, each with the file, the line number and the reason; empty if there are none
     */
    List<String> getAvertismenteIncarcare();
}
