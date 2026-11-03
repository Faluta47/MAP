package ro.ubb.map.retea.repository.db;

import ro.ubb.map.retea.domain.Utilizator;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.validators.Validator;

import java.util.Optional;

/**
 * Database repository skeleton for users. Every operation throws
 * {@link UnsupportedOperationException} until it is implemented (Lab 4).
 */
public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private Validator<Utilizator> validator;
    private String url;
    private String user;
    private String password;

    /**
     * Creates the repository skeleton; no connection is opened.
     *
     * @param validator the validator of the entities
     * @param url       the database url (db.url)
     * @param user      the database user (db.user)
     * @param password  the database password (db.password)
     */
    public UtilizatorDbRepository(Validator<Utilizator> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Not implemented yet.
     *
     * @param id the id to look for
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Optional<Utilizator> findOne(Long id) {
        throw new UnsupportedOperationException("findOne is not implemented yet (Lab 4)");
    }

    /**
     * Not implemented yet.
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Iterable<Utilizator> findAll() {
        throw new UnsupportedOperationException("findAll is not implemented yet (Lab 4)");
    }

    /**
     * Not implemented yet.
     *
     * @param entity the entity to save
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        throw new UnsupportedOperationException("save is not implemented yet (Lab 4)");
    }

    /**
     * Not implemented yet.
     *
     * @param id the id of the entity to delete
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Optional<Utilizator> delete(Long id) {
        throw new UnsupportedOperationException("delete is not implemented yet (Lab 4)");
    }

    /**
     * Not implemented yet.
     *
     * @param entity the new version of the entity
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Optional<Utilizator> update(Utilizator entity) {
        throw new UnsupportedOperationException("update is not implemented yet (Lab 4)");
    }
}
