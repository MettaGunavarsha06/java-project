package com.quiz.util;

import com.quiz.annotations.AuditLog;
import com.quiz.annotations.Author;
import com.quiz.annotations.RoleRequired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility demonstrating Java Reflection API.
 * Demonstrates:
 * - Dynamic class inspection at runtime (Class.forName, getDeclaredMethods, getFields)
 * - Checking modifiers, method signatures, return types
 * - Inspecting custom runtime annotations (@Author, @RoleRequired, @AuditLog)
 */
public class ReflectionInspector {

    public static String inspectClass(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("REFLECTION INSPECTION: ").append(clazz.getName()).append("\n");
        sb.append("============================================================\n");

        // Inspect Class-level Annotations
        sb.append("[Class Annotations]:\n");
        for (Annotation ann : clazz.getAnnotations()) {
            sb.append("  @").append(ann.annotationType().getSimpleName());
            if (ann instanceof Author) {
                Author a = (Author) ann;
                sb.append(String.format("(name='%s', date='%s', version='%s')", a.name(), a.date(), a.version()));
            } else if (ann instanceof RoleRequired) {
                RoleRequired r = (RoleRequired) ann;
                sb.append(String.format("(role='%s')", r.value()));
            }
            sb.append("\n");
        }

        // Inspect Constructors
        sb.append("\n[Constructors]:\n");
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            sb.append("  ").append(Modifier.toString(c.getModifiers())).append(" ")
              .append(c.getName()).append("(");
            Class<?>[] params = c.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i].getSimpleName());
                if (i < params.length - 1) sb.append(", ");
            }
            sb.append(")\n");
        }

        // Inspect Fields
        sb.append("\n[Declared Fields]:\n");
        for (Field f : clazz.getDeclaredFields()) {
            sb.append("  ").append(Modifier.toString(f.getModifiers())).append(" ")
              .append(f.getType().getSimpleName()).append(" ")
              .append(f.getName()).append("\n");
        }

        // Inspect Methods & Audit Annotations
        sb.append("\n[Declared Methods]:\n");
        for (Method m : clazz.getDeclaredMethods()) {
            sb.append("  ").append(Modifier.toString(m.getModifiers())).append(" ")
              .append(m.getReturnType().getSimpleName()).append(" ")
              .append(m.getName()).append("(");
            Class<?>[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i].getSimpleName());
                if (i < params.length - 1) sb.append(", ");
            }
            sb.append(")");

            if (m.isAnnotationPresent(AuditLog.class)) {
                AuditLog audit = m.getAnnotation(AuditLog.class);
                sb.append(" --> [@AuditLog: ").append(audit.action()).append("]");
            }
            sb.append("\n");
        }
        sb.append("============================================================\n");
        return sb.toString();
    }

    public static List<String> getAuditedMethods(Class<?> clazz) {
        List<String> list = new ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(AuditLog.class)) {
                AuditLog audit = m.getAnnotation(AuditLog.class);
                list.add(m.getName() + " -> " + audit.action() + " (Critical: " + audit.critical() + ")");
            }
        }
        return list;
    }
}

