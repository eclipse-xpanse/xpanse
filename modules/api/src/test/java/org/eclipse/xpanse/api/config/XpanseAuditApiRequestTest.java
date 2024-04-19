package org.eclipse.xpanse.api.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class XpanseAuditApiRequestTest {

    public static final String ANNOTATED_METHOD_PACKAGE_NAME = "org.eclipse.xpanse.api.controllers";

    @Test
    void testAllAnnotatedMethodsHaveAuditApiRequest() {
        List<Method> annotatedMethods = getAnnotatedMethods(ANNOTATED_METHOD_PACKAGE_NAME);

        for (Method method : annotatedMethods) {
            assertTrue(method.isAnnotationPresent(AuditApiRequest.class),
                    "Class [" + method.getDeclaringClass().getName() + "],Method ["
                            + method.getName() + "] does not have @AuditApiRequest annotation.");
        }
    }

    private List<Method> getAnnotatedMethods(String packageName) {
        List<Method> annotatedMethods = new ArrayList<>();
        List<Class<?>> classes = getClasses(packageName);
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(PostMapping.class) ||
                        method.isAnnotationPresent(GetMapping.class) ||
                        method.isAnnotationPresent(PutMapping.class) ||
                        method.isAnnotationPresent(DeleteMapping.class) ||
                        method.isAnnotationPresent(PatchMapping.class) ||
                        method.isAnnotationPresent(RequestMapping.class)) {
                    annotatedMethods.add(method);
                }
            }
        }
        return annotatedMethods;
    }

    private List<Class<?>> getClasses(String packageName) {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ArrayList<Class<?>> classes = new ArrayList<>();
        try {
            File dir = new File(classLoader.getResource(path).getFile());
            for (File file : dir.listFiles()) {
                String fileName = file.getName();
                if (fileName.endsWith(".class")) {
                    classes.add(Class.forName(
                            packageName + '.' + fileName.substring(0, fileName.length() - 6)));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
    }
}