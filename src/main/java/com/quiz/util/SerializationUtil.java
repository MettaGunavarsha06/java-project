package com.quiz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility for Java Object Serialization and Deserialization.
 * Demonstrates:
 * - Serialization concept: ObjectOutputStream & ObjectInputStream
 * - Checked Exception handling (IOException, ClassNotFoundException)
 * - Safe file I/O operations with try-with-resources
 */
public class SerializationUtil {

    public static void serializeToFile(Object object, String filePath) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("Cannot serialize a null object");
        }
        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(targetFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
            oos.flush();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromFile(String filePath, Class<T> clazz) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Target file does not exist for deserialization: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (clazz.isInstance(obj)) {
                return (T) obj;
            } else {
                throw new ClassCastException("Expected instance of " + clazz.getName() + " but found " + obj.getClass().getName());
            }
        }
    }
}

