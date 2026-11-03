package ro.ubb.map.retea.domain;

import java.time.LocalDateTime;

/**
 * A friendship between two users. The id is the pair (smallerId, largerId), so the
 * friendship 1-2 is identical to 2-1.
 */
public class Prietenie extends Entity<Tuple<Long, Long>> {
    private LocalDateTime data;

    /**
     * Creates a normalized friendship dated now (without fractions of a second).
     *
     * @param idUnu the id of one user
     * @param idDoi the id of the other user
     */
    public Prietenie(Long idUnu, Long idDoi) {
        this(idUnu, idDoi, LocalDateTime.now().withNano(0));
    }

    /**
     * Creates a normalized friendship (smaller id first). Null ids are kept as they are,
     * so that the validator can reject them.
     *
     * @param idUnu the id of one user
     * @param idDoi the id of the other user
     * @param data  the date the friendship started
     */
    public Prietenie(Long idUnu, Long idDoi, LocalDateTime data) {
        if (idUnu != null && idDoi != null && idUnu > idDoi) {
            setId(new Tuple<Long, Long>(idDoi, idUnu));
        } else {
            setId(new Tuple<Long, Long>(idUnu, idDoi));
        }
        this.data = data;
    }

    /**
     * Returns the smaller of the two user ids.
     *
     * @return the smaller user id
     */
    public Long getIdUtilizator1() {
        return getId().getFirst();
    }

    /**
     * Returns the larger of the two user ids.
     *
     * @return the larger user id
     */
    public Long getIdUtilizator2() {
        return getId().getSecond();
    }

    /**
     * Returns the date of the friendship.
     *
     * @return the date of the friendship
     */
    public LocalDateTime getData() {
        return data;
    }

    /**
     * Sets the date of the friendship.
     *
     * @param data the new date
     */
    public void setData(LocalDateTime data) {
        this.data = data;
    }

    /**
     * Returns a readable form of the friendship.
     *
     * @return a readable form of the friendship
     */
    @Override
    public String toString() {
        return getIdUtilizator1() + " - " + getIdUtilizator2() + " (din " + data + ")";
    }
}
