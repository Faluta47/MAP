package ro.ubb.map.retea.repository.file;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository that persists the entities in a text file, one per line (Template Method pattern:
 * the read/write skeleton is here, the subclasses only provide the line - entity conversion).
 * The file is loaded lazily, on the first operation, and rewritten after every successful
 * save, delete or update. Corrupt lines are reported to {@link LoadWarnings} and skipped.
 *
 * @param <ID> the type of the id
 * @param <E>  the type of the entity
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    private Path path;
    private LoadWarnings warnings;
    private boolean loaded = false;

    /**
     * Creates the repository without reading the file (loading is lazy, see Effective Java, Item 19:
     * no overridable method is called from the constructor).
     *
     * @param validator the validator of the entities
     * @param fileName  the path of the file
     * @param warnings  the collector that receives the warnings produced while loading
     */
    public AbstractFileRepository(Validator<E> validator, String fileName, LoadWarnings warnings) {
        super(validator);
        this.path = Paths.get(fileName);
        this.warnings = warnings;
    }

    /**
     * Builds an entity from a file line.
     *
     * @param linie the line that was read
     * @return the corresponding entity
     * @throws RepositoryException if the line does not have the expected format; the cause is the
     *                             NumberFormatException or DateTimeParseException when there is one
     */
    protected abstract E extractEntity(String linie);

    /**
     * Serializes an entity into a file line.
     *
     * @param entity the entity
     * @return the line to write
     */
    protected abstract String createEntityAsString(E entity);

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        List<String> linii = readLines();
        loaded = true;
        for (int i = 0; i < linii.size(); i++) {
            String linie = linii.get(i);
            if (!linie.trim().isEmpty()) {
                incarcaLinie(linie, i + 1);
            }
        }
    }

    /** Reads the whole file; a missing file means an empty repository. */
    private List<String> readLines() {
        List<String> linii = new ArrayList<String>();
        if (!Files.exists(path)) {
            return linii;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))) {
            String linie = reader.readLine();
            while (linie != null) {
                linii.add(linie);
                linie = reader.readLine();
            }
        } catch (IOException e) {
            throw new RepositoryException("Nu se poate citi fisierul " + path, e);
        }
        return linii;
    }

    /** Loads one line; a bad line becomes a warning and the loading goes on. */
    private void incarcaLinie(String linie, int numarLinie) {
        try {
            E entity = extractEntity(linie);
            // super.save: the in-memory save, so the file is not rewritten while it is being read
            if (super.save(entity) != null) {
                warnings.add(descriere(numarLinie, "id duplicat", linie));
            }
        } catch (RepositoryException | ValidationException e) {
            warnings.add(descriere(numarLinie, e.getMessage(), linie));
        }
    }

    private String descriere(int numarLinie, String motiv, String linie) {
        return path.getFileName() + ", linia " + numarLinie + ": " + motiv + " (continut: \"" + linie + "\")";
    }

    private void writeData() {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), StandardCharsets.UTF_8))) {
                for (E entity : super.findAll()) {
                    writer.write(createEntityAsString(entity));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Nu se poate scrie fisierul " + path, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public E findOne(ID id) {
        ensureLoaded();
        return super.findOne(id);
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<E> findAll() {
        ensureLoaded();
        return super.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public E save(E entity) {
        ensureLoaded();
        E rezultat = super.save(entity);
        if (rezultat == null) {
            writeData();
        }
        return rezultat;
    }

    /** {@inheritDoc} */
    @Override
    public E delete(ID id) {
        ensureLoaded();
        E rezultat = super.delete(id);
        if (rezultat != null) {
            writeData();
        }
        return rezultat;
    }

    /** {@inheritDoc} */
    @Override
    public E update(E entity) {
        ensureLoaded();
        E rezultat = super.update(entity);
        if (rezultat == null) {
            writeData();
        }
        return rezultat;
    }
}
