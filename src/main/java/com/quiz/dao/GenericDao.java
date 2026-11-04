package com.quiz.dao;

import com.quiz.exception.DatabaseException;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) interface.
 * Demonstrates:
 * - Generics: Parameterized types <T, ID>
 * - Checked Exception handling with DatabaseException
 * - Standard CRUD contract
 */
public interface GenericDao<T, ID> {
    T save(T entity) throws DatabaseException;
    Optional<T> findById(ID id) throws DatabaseException;
    List<T> findAll() throws DatabaseException;
    boolean update(T entity) throws DatabaseException;
    boolean deleteById(ID id) throws DatabaseException;
}

