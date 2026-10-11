package com.guvi.frauddetection.common;

import java.util.Optional;

/**
 * Generic repository interface defining standard CRUD operations.
 * Demonstrates Java Generics and Repository pattern abstraction.
 *
 * @param <T>  The domain entity type
 * @param <ID> The entity identifier type
 */
public interface Repository<T, ID> {

    /**
     * Saves or updates an entity.
     *
     * @param entity the entity to persist
     * @return the persisted entity
     */
    T save(T entity);

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id the unique identifier
     * @return an {@link Optional} containing the entity if found, or empty
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities of type {@code T}.
     *
     * @return an iterable collection of entities
     */
    Iterable<T> findAll();

    /**
     * Deletes an entity by its unique identifier.
     *
     * @param id the identifier of the entity to delete
     * @return true if deleted, false otherwise
     */
    boolean deleteById(ID id);

    /**
     * Checks if an entity exists with the specified identifier.
     *
     * @param id the unique identifier
     * @return true if entity exists, false otherwise
     */
    boolean existsById(ID id);

    /**
     * Counts the total number of entities.
     *
     * @return total count
     */
    long count();
}
