package ro.ubb.map.retea.domain;

import java.util.Objects;

/**
 * Ordered pair of two values, used as a composite identifier.
 *
 * @param <E1> the type of the first value
 * @param <E2> the type of the second value
 */
public class Tuple<E1, E2> {
    private E1 first;
    private E2 second;

    /**
     * Creates a pair.
     *
     * @param first  the first value
     * @param second the second value
     */
    public Tuple(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value.
     *
     * @return the first value
     */
    public E1 getFirst() {
        return first;
    }

    /**
     * Sets the first value.
     *
     * @param first the new first value
     */
    public void setFirst(E1 first) {
        this.first = first;
    }

    /**
     * Returns the second value.
     *
     * @return the second value
     */
    public E2 getSecond() {
        return second;
    }

    /**
     * Sets the second value.
     *
     * @param second the new second value
     */
    public void setSecond(E2 second) {
        this.second = second;
    }

    /**
     * Two pairs are equal if both components are equal.
     *
     * @param o the object to compare with
     * @return true if the objects are equal; false for null or for an object of another class
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tuple)) {
            return false;
        }
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
    }

    /**
     * Hash code computed from both components, consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    /**
     * Returns the text form "(first, second)".
     *
     * @return the text form of the pair
     */
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
