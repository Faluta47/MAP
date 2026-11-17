package ro.ubb.map.retea.repository.db;

/**
 * Maps the SQLState of a failed database operation to a message that can be shown to the user.
 * It does not depend on JDBC, so it can be tested without a database. The messages never contain the
 * password or the stack trace.
 */
public final class SqlStateMessages {
    static final String UNAVAILABLE =
            "Baza de date nu este disponibila. Verifica serverul PostgreSQL si configurarea (db.url).";
    static final String AUTHENTICATION =
            "Autentificare esuata la baza de date: user sau parola gresita (verifica db.user si db.password).";
    static final String FOREIGN_KEY = "Utilizator inexistent: prietenia trebuie sa lege doi utilizatori existenti.";
    static final String DUPLICATE = "Prietenia exista deja.";
    static final String CHECK = "Prietenia este invalida: se cere id1 < id2.";

    private SqlStateMessages() {
    }

    /**
     * Builds the message for a database error.
     *
     * @param sqlState the SQLState of the error; may be null
     * @param cause    the message of the original error, used for the unrecognized states; may be null
     * @return the message for the user
     */
    public static String toMessage(String sqlState, String cause) {
        String state = sqlState == null ? "" : sqlState;
        if (state.startsWith("08")) {
            return UNAVAILABLE;
        }
        if (state.startsWith("28")) {
            return AUTHENTICATION;
        }
        if (state.equals("23503")) {
            return FOREIGN_KEY;
        }
        if (state.equals("23505")) {
            return DUPLICATE;
        }
        if (state.equals("23514")) {
            return CHECK;
        }
        return "Eroare la accesarea bazei de date" + (state.isEmpty() ? "" : " (" + state + ")")
                + (cause == null ? "." : ": " + cause);
    }
}
