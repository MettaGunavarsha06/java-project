package com.quiz.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to specify required user role for executing controller actions.
 * Demonstrates annotation metadata for security enforcement and reflection checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RoleRequired {
    String value() default "STUDENT";
}

