package org.eclipse.xpanse.modules.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Slf4j
public class DatabaseValidationTest {

    private static final String DATABASE_BASE_PACKAGE = "org.eclipse.xpanse.modules.database";

    private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("^[A-Z]+(_[A-Z]+)*$");

    /** Test to check if any repository class has the method named findAll() in its hierarchy. */
    @Test
    void checkMethodFindAllInRepositories() {
        List<String> allErrors = new ArrayList<>();
        Set<Class<?>> repositoryClasses = scanClassesWithAnnotation(Repository.class);
        repositoryClasses.forEach(
                clazz -> {
                    if (hasFindAllInHierarchy(clazz)) {
                        List<String> errors = new ArrayList<>();
                        errors.add(
                                String.format(
                                        "Class %s inherits method findAll() from hierarchy",
                                        clazz.getName()));
                        checkClassHasMethodFindAllInheritance(clazz, errors);
                        allErrors.addAll(errors);
                    }
                });
        if (!CollectionUtils.isEmpty(allErrors)) {
            Assertions.fail(formatErrorMessages(allErrors));
        }
    }

    /** Test to check if any entity class has invalid column names. */
    @Test
    void validateColumnNamesInEntityClasses() {
        List<String> allErrors = new ArrayList<>();
        Set<Class<?>> entityClasses = scanClassesWithAnnotation(Entity.class);
        entityClasses.forEach(
                entity -> {
                    List<String> errors = new ArrayList<>();
                    validateColumnNamesInEntity(entity, errors);
                    allErrors.addAll(errors);
                });
        if (!CollectionUtils.isEmpty(allErrors)) {
            Assertions.fail(formatErrorMessages(allErrors));
        }
    }

    private Set<Class<?>> scanClassesWithAnnotation(Class<? extends Annotation> annotation) {
        Set<Class<?>> classes = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotation, true, true));
        Set<BeanDefinition> beanDefinitions =
                scanner.findCandidateComponents(DATABASE_BASE_PACKAGE);
        log.info(
                "Found {} beans with annotation @{} from candidate components.",
                beanDefinitions.size(),
                annotation.getSimpleName());
        beanDefinitions.forEach(
                beanDefinition -> {
                    try {
                        classes.add(Class.forName(beanDefinition.getBeanClassName()));
                    } catch (ClassNotFoundException e) {
                        log.error(
                                "Failed to load class by bean class name {}",
                                beanDefinition.getBeanClassName(),
                                e);
                    }
                });

        // if no classes found by beans components, try to load classes from reflections.
        if (CollectionUtils.isEmpty(classes)) {
            Set<Class<?>> classesWithAnnotation =
                    new Reflections(DATABASE_BASE_PACKAGE).getTypesAnnotatedWith(annotation);
            log.info(
                    "Found {} classes with annotation @{} from classes reflections.",
                    classesWithAnnotation.size(),
                    annotation.getSimpleName());
            classes.addAll(classesWithAnnotation);
        }
        log.info(
                "Found {} classes with annotation @{}.",
                classes.size(),
                annotation.getSimpleName());
        return classes;
    }

    void checkClassHasMethodFindAllInheritance(Class<?> clazz, List<String> errors) {
        if (hasDeclaredMethodFindAll(clazz)) {
            errors.add(
                    String.format("Class %s could not declare method findAll()", clazz.getName()));
            return;
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            if (hasFindAllInHierarchy(superClass)) {
                errors.add(
                        String.format(
                                "Class %s could not extend class %s has declared method findAll()",
                                clazz.getName(), superClass.getSimpleName()));
            }
        }
        for (Class<?> interfaceClass : clazz.getInterfaces()) {
            if (hasFindAllInHierarchy(interfaceClass)) {
                errors.add(
                        String.format(
                                "Class %s cloud not implements interface %s has declared method"
                                        + " findAll()",
                                clazz.getName(), interfaceClass.getSimpleName()));
            }
        }
    }

    boolean hasFindAllInHierarchy(Class<?> clazz) {
        try {
            Method method = clazz.getMethod("findAll");
            return Modifier.isPublic(method.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    boolean hasDeclaredMethodFindAll(Class<?> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("findAll");
            return Modifier.isPublic(method.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean isEntityClass(Class<?> clazz) {
        Optional<Annotation> classHasEntityAnnotation =
                Arrays.stream(clazz.getAnnotations())
                        .filter(annotation -> annotation.annotationType() == Entity.class)
                        .findAny();
        return classHasEntityAnnotation.isPresent();
    }

    private void validateColumnNamesInEntity(Class<?> entityClass, List<String> errors) {

        for (Field field : entityClass.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    || java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            Annotation[] annotations = field.getAnnotations();
            Optional<Annotation> columnAnnotation =
                    Arrays.stream(annotations)
                            .filter(annotation -> annotation.annotationType() == Column.class)
                            .findAny();
            if (columnAnnotation.isPresent()) {
                Column column = (Column) columnAnnotation.get();
                if (StringUtils.isBlank(column.name())) {
                    errors.add(
                            String.format(
                                    "%s.%s: name set in annotation @Column cloud not be empty.",
                                    entityClass.getSimpleName(), field.getName()));
                }
                if (!COLUMN_NAME_PATTERN.matcher(column.name()).matches()) {
                    errors.add(
                            String.format(
                                    "%s.%s: format of name set in annotation @Column is invalid.",
                                    entityClass.getSimpleName(), field.getName()));
                }
            }
        }
    }

    private String formatErrorMessages(List<String> errors) {
        return String.format(
                "Found %d validation error(s):\n• %s", errors.size(), String.join("\n• ", errors));
    }
}
