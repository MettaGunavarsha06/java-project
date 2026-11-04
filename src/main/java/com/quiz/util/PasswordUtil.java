package com.quiz.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing and verifying user passwords using SHA-256.
 * Demonstrates:
 * - Secure credential handling
 * - Checked Exception handling (NoSuchAlgorithmException)
 * - Byte array and hexadecimal conversion
 */
public class PasswordUtil {

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        // If storedHash is already a 64-char hex SHA-256 hash
        if (storedHash.length() == 64) {
            String computedHash = hashPassword(rawPassword);
            return computedHash.equalsIgnoreCase(storedHash);
        }
        // Fallback for raw match if not hashed in legacy seed
        return rawPassword.equals(storedHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

