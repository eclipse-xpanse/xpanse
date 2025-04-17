/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.runtime.testContainers.ZitadelTestContainer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "mysql")
public abstract class AbstractMysqlIntegrationTest extends ZitadelTestContainer {

    private static final MySQLContainer<?> mysqlContainer;

    static {
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:9.2.0"));
        mysqlContainer.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.jdbcUrl", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @DynamicPropertySource
    static void activitiProps(DynamicPropertyRegistry registry) {
        registry.add("spring.activiti.datasource.jdbcUrl", mysqlContainer::getJdbcUrl);
        registry.add("spring.activiti.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.activiti.datasource.password", mysqlContainer::getPassword);
    }
}
