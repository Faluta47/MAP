package ro.ubb.map.retea.repository.factory;

/**
 * The persistence types supported by {@link RepositoryFactory}.
 */
public enum RepositoryType {
    /** Data kept only in memory. */
    MEMORY,
    /** Data persisted in text files. */
    FILE,
    /** Data persisted in a database (skeleton, implemented at Lab 5). */
    DATABASE
}
