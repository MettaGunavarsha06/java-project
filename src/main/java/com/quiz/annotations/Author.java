package com.quiz.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark the developer/author of classes or methods.
 * Demonstrates Java Custom Annotations with RetentionPolicy.RUNTIME
 * so it can be inspected via Java Reflection API.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Author {
    String name() default "Antigravity Dev";
    String date() default "2026-11-03";
    String version() default "1.0.0";
}

