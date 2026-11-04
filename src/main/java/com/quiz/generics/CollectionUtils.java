package com.quiz.generics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class demonstrating:
 * - Generics: Generic methods <T>, <K, V>
 * - Collections: List, Set (HashSet), Map (HashMap)
 * - Sorting with Comparator & Lambdas
 * - Searching algorithms
 * - Stream API operations
 */
public class CollectionUtils {

    /**
     * Generic method to sort a list using a custom Comparator.
     */
    public static <T> List<T> sortList(List<T> list, Comparator<? super T> comparator) {
        if (list == null) return new ArrayList<>();
        List<T> copy = new ArrayList<>(list);
        copy.sort(comparator);
        return copy;
    }

    /**
     * Generic search method that finds the first element matching a predicate.
     */
    public static <T> T findFirst(List<T> list, Predicate<T> predicate) {
        if (list == null || predicate == null) return null;
        for (T item : list) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Extracts unique elements into a HashSet.
     */
    public static <T, R> Set<R> extractUnique(List<T> list, Function<T, R> mapper) {
        if (list == null) return new HashSet<>();
        Set<R> uniqueSet = new HashSet<>();
        for (T item : list) {
            uniqueSet.add(mapper.apply(item));
        }
        return uniqueSet;
    }

    /**
     * Groups a list into a Map using a key extractor.
     */
    public static <K, V> Map<K, List<V>> groupBy(List<V> list, Function<V, K> keyExtractor) {
        if (list == null) return Collections.emptyMap();
        return list.stream().collect(Collectors.groupingBy(keyExtractor));
    }
}

