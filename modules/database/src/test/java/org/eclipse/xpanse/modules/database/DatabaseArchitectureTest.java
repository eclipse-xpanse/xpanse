package org.eclipse.xpanse.modules.database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@Slf4j
public class DatabaseArchitectureTest {

    private static final String DATABASE_PACKAGE_NAME = "org.eclipse.xpanse.modules.database";

    /**
     * Check if any class in the repositories have the method findAll().
     *
     * @throws IOException if the classloader cannot find the resource.
     */
    @Test
    void checkMethodFindAllInRepositories() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = DATABASE_PACKAGE_NAME.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            if (file.getPath().contains("test-classes")) {
                continue;
            }
            if (resource.getProtocol().equals("file")) {
                scanDirectoryForClasses(new File(resource.getFile()), DATABASE_PACKAGE_NAME);
            }
        }
    }

    void scanDirectoryForClasses(File directory, String packageName) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectoryForClasses(file, packageName + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName()
                            .substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        checkClazzHasFindAllMethod(clazz, className);
                    } catch (ClassNotFoundException e) {
                        log.error("Failed to load class {}", className, e);
                    }
                }
            }
        }
    }

    void checkClazzHasFindAllMethod(Class<?> clazz, String subClassName) {
        if (hasMethodFindAll(clazz)) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> face : interfaces) {
                if (hasMethodFindAll(face)) {
                    Assertions.fail("Class " + subClassName + " should not implement the interface "
                            + face.getName() + " which declare the method findAll().");
                }
            }
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                if (hasMethodFindAll(superclass)) {
                    Assertions.fail(
                            "Class " + subClassName + " should not extend the class "
                                    + clazz.getName()
                                    + " which declare the method findAll().");
                }
            }
            Assertions.fail("Class " + subClassName + " should not declare method findAll().");
        }
    }

    boolean hasMethodFindAll(Class<?> clazz) {
        try {
            clazz.getMethod("findAll");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}

