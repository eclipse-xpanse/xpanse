package org.eclipse.xpanse.api.annotations;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
public class AgentAnnotationTest {

    @Test
    void testRestControllersAnnotations() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents("org.eclipse.xpanse");

        for (BeanDefinition beanDefinition : beanDefinitions) {
            try {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                log.info("Checking RestController: {}", clazz.getName());
                if (!clazz.getName().equals("org.eclipse.xpanse.api.controllers.AgentPollingApi")) {
                    Annotation[] annotations = clazz.getAnnotations();
                    Assertions.assertTrue(
                            Arrays.stream(annotations)
                                    .anyMatch(
                                            annotation ->
                                                    annotation instanceof ConditionalOnProperty),
                            String.format(
                                    "ConditionalOnProperty annotation missing on %s ",
                                    clazz.getName()));
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof ConditionalOnProperty) {
                            Assertions.assertEquals(
                                    1,
                                    ((ConditionalOnProperty) annotation).name().length,
                                    String.format(
                                            "no name found in the ConditionalOnProperty annotation"
                                                    + " in class %s.",
                                            clazz.getName()));
                            Assertions.assertEquals(
                                    "false",
                                    ((ConditionalOnProperty) annotation).havingValue(),
                                    String.format(
                                            "%s has wrong conditional property", clazz.getName()));
                            Assertions.assertTrue(
                                    Arrays.asList(((ConditionalOnProperty) annotation).name())
                                            .contains("xpanse.agent-api.enable-agent-api-only"),
                                    "xpanse.agent-api.enable-agent-api-only property missing on"
                                            + " class "
                                            + clazz.getName());
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
