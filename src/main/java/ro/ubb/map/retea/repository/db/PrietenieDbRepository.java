package ro.ubb.map.retea.repository.db;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.validators.Validator;

import java.util.Optional;

/**
 * Database repository skeleton for friendships. Every operation throws
 * {@link UnsupportedOperationException} until it is implemented (Lab 4).
 */
public class PrietenieDbRepository implements Repository<Tuple<Long, Long>, Prietenie> {
    private Validator<Prietenie> validator;
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
    public PrietenieDbRepository(Validator<Prietenie> validator, String url, String user, String password) {
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
    public Optional<Prietenie> findOne(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException("findOne is not implemented yet (Lab 4)");
    }

    /**
     * Not implemented yet.
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Iterable<Prietenie> findAll() {
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
    public Optional<Prietenie> save(Prietenie entity) {
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
    public Optional<Prietenie> delete(Tuple<Long, Long> id) {
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
    public Optional<Prietenie> update(Prietenie entity) {
        throw new UnsupportedOperationException("update is not implemented yet (Lab 4)");
    }
}
