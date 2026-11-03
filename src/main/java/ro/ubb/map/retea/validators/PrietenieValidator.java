package ro.ubb.map.retea.validators;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.Repository;

/**
 * Validator for {@link Prietenie}: non-null and different ids, referring to existing users.
 * Existence is checked here (and not only in the service) so that the rule also holds when the
 * data is loaded from a file, a path that does not go through the service.
 */
public class PrietenieValidator implements Validator<Prietenie> {
    private Repository<Long, Utilizator> repoUtilizatori;

    /**
     * Creates a validator that does not check the existence of the users.
     */
    public PrietenieValidator() {
        this(null);
    }

    /**
     * Creates a validator that also checks that both users exist.
     *
     * @param repoUtilizatori the repository used to look the users up; null disables the existence check
     */
    public PrietenieValidator(Repository<Long, Utilizator> repoUtilizatori) {
        this.repoUtilizatori = repoUtilizatori;
    }

    /**
     * Validates the friendship and gathers all the errors in a single message.
     *
     * @param p the friendship to validate
     * @throws ValidationException if the ids are missing or equal, or if a user does not exist
     */
    @Override
    public void validate(Prietenie p) {
        StringBuilder erori = new StringBuilder();
        if (p.getIdUtilizator1() == null) {
            erori.append("Primul id de utilizator nu poate fi null. ");
        }
        if (p.getIdUtilizator2() == null) {
            erori.append("Al doilea id de utilizator nu poate fi null. ");
        }
        if (p.getIdUtilizator1() != null && p.getIdUtilizator1().equals(p.getIdUtilizator2())) {
            erori.append("Un utilizator nu poate fi prieten cu el insusi. ");
        }
        if (erori.length() == 0) {
            verificaExista(p.getIdUtilizator1(), erori);
            verificaExista(p.getIdUtilizator2(), erori);
        }
        if (erori.length() > 0) {
            throw new ValidationException(erori.toString().trim());
        }
    }

    private void verificaExista(Long id, StringBuilder erori) {
        if (repoUtilizatori != null && repoUtilizatori.findOne(id) == null) {
            erori.append("Utilizatorul ").append(id).append(" nu exista. ");
        }
    }
}
