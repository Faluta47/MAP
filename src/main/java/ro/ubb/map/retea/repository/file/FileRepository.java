package ro.ubb.map.retea.repository.file;

import ro.ubb.map.retea.domain.Entity;
import ro.ubb.map.retea.exceptions.RepositoryException;
import ro.ubb.map.retea.exceptions.ValidationException;
import ro.ubb.map.retea.repository.LoadWarnings;
import ro.ubb.map.retea.repository.Repository;
import ro.ubb.map.retea.repository.memory.InMemoryRepository;
import ro.ubb.map.retea.validators.Validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Repository that persists the entities in a text file, one per line. It is built by composition:
 * the line - entity conversion is given as two functions (see {@link UtilizatorConverter} and
 * {@link PrietenieConverter}) and the entities are kept by an {@link InMemoryRepository} delegate.
 * The file is loaded lazily, on the first operation, and rewritten after every successful
 * save, delete or update. Corrupt lines are reported to {@link LoadWarnings} and skipped.
 *
 * @param <ID> the type of the id
 * @param <E>  the type of the entity
 */
public class FileRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    private final Path path;
    private final InMemoryRepository<ID, E> memorie;
    private final Function<String, E> parser;
    private final Function<E, String> formatter;
    private final LoadWarnings warnings;
    private boolean loaded = false;

    /**
     * Creates the repository without reading the file (loading is lazy).
     *
     * @param fileName  the path of the file
     * @param validator the validator of the entities
     * @param parser    builds an entity from a line; throws {@link RepositoryException} if the line is not valid
     * @param formatter serializes an entity into a line
     * @param warnings  the collector that receives the warnings produced while loading
     */
    public FileRepository(String fileName, Validator<E> validator, Function<String, E> parser,
                          Function<E, String> formatter, LoadWarnings warnings) {
        this.path = Paths.get(fileName);
        this.memorie = new InMemoryRepository<>(validator);
        this.parser = parser;
        this.formatter = formatter;
        this.warnings = warnings;
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        List<String> linii = readLines();
        loaded = true;
        // the index is needed to report the line number of the corrupt lines
        IntStream.range(0, linii.size())
                .filter(i -> !linii.get(i).trim().isEmpty())
                .forEach(i -> incarcaLinie(linii.get(i), i + 1));
    }

    /** Reads the whole file; a missing file means an empty repository. */
    private List<String> readLines() {
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        // the stream keeps the file open, so it is closed here; its content is collected before leaving
        try (Stream<String> linii = Files.lines(path, StandardCharsets.UTF_8)) {
            return linii.collect(Collectors.toList());
        } catch (IOException e) {
            throw new RepositoryException("Nu se poate citi fisierul " + path, e);
        } catch (UncheckedIOException e) {
            // Files.lines wraps the IOException thrown while reading
            throw new RepositoryException("Nu se poate citi fisierul " + path, e.getCause());
        }
    }

    /** Loads one line; a bad line becomes a warning and the loading goes on. */
    private void incarcaLinie(String linie, int numarLinie) {
        try {
            // the in-memory save, so the file is not rewritten while it is being read
            memorie.save(parser.apply(linie))
                    .ifPresent(existent -> warnings.add(descriere(numarLinie, "id duplicat", linie)));
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
            List<String> linii = memorie.stream().map(formatter).collect(Collectors.toList());
            Files.write(path, linii, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Nu se poate scrie fisierul " + path, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> findOne(ID id) {
        ensureLoaded();
        return memorie.findOne(id);
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<E> findAll() {
        ensureLoaded();
        return memorie.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> save(E entity) {
        ensureLoaded();
        Optional<E> rezultat = memorie.save(entity);
        if (!rezultat.isPresent()) {
            writeData();
        }
        return rezultat;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> delete(ID id) {
        ensureLoaded();
        Optional<E> rezultat = memorie.delete(id);
        if (rezultat.isPresent()) {
            writeData();
        }
        return rezultat;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<E> update(E entity) {
        ensureLoaded();
        Optional<E> rezultat = memorie.update(entity);
        if (!rezultat.isPresent()) {
            writeData();
        }
        return rezultat;
    }
}
