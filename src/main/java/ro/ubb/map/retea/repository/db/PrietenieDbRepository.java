package ro.ubb.map.retea.repository.db;

import ro.ubb.map.retea.domain.Prietenie;
import ro.ubb.map.retea.domain.Tuple;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.validators.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL repository for friendships (table {@code friendships}). The id of a friendship is the pair
 * (id1, id2) with id1 &lt; id2, already normalized by the domain. All the statements are
 * {@link PreparedStatement}s with {@code ?} parameters.
 */
public class PrietenieDbRepository extends AbstractDbRepository<Tuple<Long, Long>, Prietenie> {
    /**
     * Creates the repository; no connection is opened.
     *
     * @param validator the validator of the friendships
     * @param url       the database url (db.url)
     * @param user      the database user (db.user)
     * @param password  the database password (db.password)
     */
    public PrietenieDbRepository(Validator<Prietenie> validator, String url, String user, String password) {
        super(validator, url, user, password);
    }

    /**
     * {@inheritDoc}
     *
     * @throws RepositoryException if the database cannot be accessed
     */
    @Override
    public Optional<Prietenie> findOne(Tuple<Long, Long> id) {
        requireNonNull(id, "Id-ul nu poate fi null.");
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id1, id2, friends_from FROM friendships WHERE id1 = ? AND id2 = ?")) {
            setId(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(toPrietenie(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RepositoryException if the database cannot be accessed
     */
    @Override
    public Iterable<Prietenie> findAll() {
        List<Prietenie> prietenii = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("SELECT id1, id2, friends_from FROM friendships ORDER BY id1, id2");
             ResultSet rs = ps.executeQuery()) {
            // a ResultSet is a cursor, not a collection: it can only be traversed with next()
            while (rs.next()) {
                prietenii.add(toPrietenie(rs));
            }
            return prietenii;
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return an empty Optional if the friendship was saved, otherwise the given entity (it already exists)
     * @throws ValidationException if the friendship is not valid
     * @throws RepositoryException if the database cannot be accessed
     */
    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        requireNonNull(entity, "Entitatea nu poate fi null.");
        validator.validate(entity);
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO friendships(id1, id2, friends_from) VALUES (?, ?, ?) ON CONFLICT (id1, id2) DO NOTHING")) {
            setId(ps, entity.getId());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getData()));
            // 0 inserted rows: the friendship already exists
            return ps.executeUpdate() == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RepositoryException if the database cannot be accessed
     */
    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> id) {
        requireNonNull(id, "Id-ul nu poate fi null.");
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM friendships WHERE id1 = ? AND id2 = ? RETURNING id1, id2, friends_from")) {
            setId(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(toPrietenie(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     * Only the date of the friendship can change.
     *
     * @throws ValidationException if the friendship is not valid
     * @throws RepositoryException if the database cannot be accessed
     */
    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        requireNonNull(entity, "Entitatea nu poate fi null.");
        validator.validate(entity);
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(entity.getData()));
            ps.setLong(2, entity.getIdUtilizator1());
            ps.setLong(3, entity.getIdUtilizator2());
            return ps.executeUpdate() == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    /** Sets the first two parameters (id1, id2) of a statement. */
    private static void setId(PreparedStatement ps, Tuple<Long, Long> id) throws SQLException {
        ps.setLong(1, id.getFirst());
        ps.setLong(2, id.getSecond());
    }

    /** The only place where a row of {@code friendships} becomes a {@link Prietenie}. */
    private static Prietenie toPrietenie(ResultSet rs) throws SQLException {
        return new Prietenie(rs.getLong("id1"), rs.getLong("id2"), rs.getTimestamp("friends_from").toLocalDateTime());
    }
}
