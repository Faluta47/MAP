package ro.ubb.map.retea.repository;

import ro.ubb.map.retea.domain.Utilizator;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Search criteria for users, as {@link Predicate}s to be used with {@link Repository#findAll(Predicate)}.
 * Text comparisons ignore the case. The friends-based criteria read {@link Utilizator#getPrieteni()},
 * which is filled by the service layer.
 */
public final class CriteriiUtilizator {
    private CriteriiUtilizator() {
    }

    /**
     * Users whose last name starts with a prefix.
     *
     * @param prefix the prefix, case-insensitive; cannot be null
     * @return the criterion
     * @throws NullPointerException if the prefix is null
     */
    public static Predicate<Utilizator> numeIncepeCu(String prefix) {
        String cautat = normalizeaza(prefix);
        return u -> normalizeaza(u.getNume()).startsWith(cautat);
    }

    /**
     * Users whose last name starts with at least one of the given prefixes (the prefixes are separated by
     * comma; blank ones are ignored). It combines {@link #numeIncepeCu(String)} criteria with {@code or}.
     *
     * @param prefixe the prefixes separated by comma, case-insensitive; cannot be null
     * @return the criterion; if there is no prefix at all, no user satisfies it
     * @throws NullPointerException if the text is null
     */
    public static Predicate<Utilizator> numeIncepeCuUnulDin(String prefixe) {
        return Arrays.stream(prefixe.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isEmpty))
                .map(CriteriiUtilizator::numeIncepeCu)
                .reduce(Predicate::or)
                .orElse(utilizator -> false);
    }

    /**
     * Users whose first name contains a text.
     *
     * @param text the text, case-insensitive; cannot be null
     * @return the criterion
     * @throws NullPointerException if the text is null
     */
    public static Predicate<Utilizator> prenumeContine(String text) {
        String cautat = normalizeaza(text);
        return u -> normalizeaza(u.getPrenume()).contains(cautat);
    }

    /**
     * Users that have exactly the given username.
     *
     * @param username the username to look for; cannot be null
     * @return the criterion
     * @throws IllegalArgumentException if the username is null
     */
    public static Predicate<Utilizator> areUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username-ul nu poate fi null.");
        }
        return u -> username.equals(u.getUsername());
    }

    /**
     * Users without friends.
     *
     * @return the criterion
     */
    public static Predicate<Utilizator> faraPrieteni() {
        return u -> u.getPrieteni().isEmpty();
    }

    /**
     * Users with at least a number of friends.
     *
     * @param minim the minimum number of friends
     * @return the criterion
     */
    public static Predicate<Utilizator> areMinimPrieteni(int minim) {
        return u -> u.getPrieteni().size() >= minim;
    }

    private static String normalizeaza(String text) {
        return Objects.requireNonNullElse(text, "").toLowerCase(Locale.ROOT);
    }
}
