package ro.ubb.map.retea.service;

import ro.ubb.map.retea.domain.Utilizator;

import java.util.List;

/**
 * Result of the search for the most sociable community: its members, the length and the longest path.
 */
public class ComunitateSociabila {
    private final List<Utilizator> membri;
    private final int lungimeDrum;
    private final List<Utilizator> drum;

    /**
     * Creates the result.
     *
     * @param membri      the members of the connected component
     * @param lungimeDrum the length of the longest elementary path, in edges
     * @param drum        the nodes of the path, in order
     */
    public ComunitateSociabila(List<Utilizator> membri, int lungimeDrum, List<Utilizator> drum) {
        this.membri = membri;
        this.lungimeDrum = lungimeDrum;
        this.drum = drum;
    }

    /**
     * Returns the members of the community.
     *
     * @return the members of the community
     */
    public List<Utilizator> getMembri() {
        return membri;
    }

    /**
     * Returns the length of the longest elementary path.
     *
     * @return the length of the longest elementary path (edges)
     */
    public int getLungimeDrum() {
        return lungimeDrum;
    }

    /**
     * Returns the users on the longest path.
     *
     * @return the users on the longest path, in order
     */
    public List<Utilizator> getDrum() {
        return drum;
    }
}
