/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import org.activiti.spring.boot.ActivitiMethodSecurityAutoConfiguration;
import org.activiti.spring.boot.ProcessEngineAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry class to xpanse runtime. This class can be directly executed to start the server.
 */
@EnableCaching
@EnableRetry
@EnableAsync(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = "org.eclipse.xpanse")
@EntityScan(basePackages = "org.eclipse.xpanse")
@ComponentScan(basePackages = "org.eclipse.xpanse")
@EnableJpaAuditing(dateTimeProviderRef = "auditDateTimeProviderConfig")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
        ProcessEngineAutoConfiguration.class, ActivitiMethodSecurityAutoConfiguration.class})
public class XpanseApplication {

    public static void main(String[] args) {
        SpringApplication.run(XpanseApplication.class, args);
    }
}