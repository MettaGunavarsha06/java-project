package com.quiz.generics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic in-memory repository demonstrating:
 * - Generics: Class-level <T, ID> and method-level type parameters
 * - Collections: ConcurrentHashMap, List, ArrayList
 * - Java 8 Functional interfaces (Predicate, Function)
 * - Java Stream API (filter, collect)
 */
public class GenericRepository<T, ID> {
    private final Map<ID, T> storage = new ConcurrentHashMap<>();
    private final Function<T, ID> idExtractor;

    public GenericRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    public T add(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        ID id = idExtractor.apply(entity);
        storage.put(id, entity);
        return entity;
    }

    public Optional<T> get(ID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    public List<T> getAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean remove(ID id) {
        return storage.remove(id) != null;
    }

    public List<T> findWhere(Predicate<T> condition) {
        return storage.values().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    public int count() {
        return storage.size();
    }

    public void clear() {
        storage.clear();
    }
}

