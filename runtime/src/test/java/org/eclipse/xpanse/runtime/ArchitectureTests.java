/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsExporter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@AnalyzeClasses(packages = "org.eclipse.xpanse")
public class ArchitectureTests {

    @ArchTest
    public static void testApiClassLocation(JavaClasses classes) {
        ArchRule archRule = classes()
                .that()
                .areAnnotatedWith(RestController.class)
                .should()
                .resideInAPackage("..api..");
        archRule.check(classes);
    }

    @ArchTest
    public static void testEntityClassLocation(JavaClasses classes) {
        ArchRule archRule = classes()
                .that()
                .areAnnotatedWith(Entity.class)
                .should()
                .resideInAPackage("..database..");
        archRule.check(classes);
    }

    @ArchTest
    public static void testExceptionHandlerClassLocation(JavaClasses classes) {
        ArchRule archRule = classes()
                .that()
                .areAnnotatedWith(RestControllerAdvice.class)
                .should()
                .resideInAPackage("..api..");
        archRule.check(classes);
    }

    @ArchTest
    public static void pluginImplementationClassLocation(JavaClasses classes) {
        ArchRule archRule = classes()
                .that()
                .implement(OrchestratorPlugin.class)
                .should()
                .resideInAPackage("..plugins..");
        archRule.check(classes);
    }

    @ArchTest
    public static void metricsExporterImplementationClassLocation(JavaClasses classes) {
        ArchRule archRule = classes()
                .that()
                .implement(ServiceMetricsExporter.class)
                .should()
                .resideInAPackage("..plugins..");
        archRule.check(classes);
    }

    @ArchTest
    public static void cacheClassLocation(JavaClasses classes) {
        ArchRule archRule = classes()
                .that()
                .haveSimpleNameContaining("Cache")
                .and().haveSimpleNameNotContaining("DeployerTool")
                .should()
                .resideInAPackage("..cache..");
        archRule.check(classes);
    }

    @ArchTest
    public static void exceptionClassLocation(JavaClasses classes) {
        ArchRule archRule = classes()
                .that()
                .areAssignableTo(RuntimeException.class)
                .should()
                .resideInAPackage("..exceptions..");
        archRule.check(classes);
    }

    @ArchTest
    public static void commonModuleShouldNotHaveDependencyToOtherModules(JavaClasses classes) {
        ArchRule archRule = noClasses().that().resideInAPackage("..common..")
                .should().dependOnClassesThat().resideInAPackage("org.eclipse.xpanse.modules");
        archRule.check(classes);
    }


}
