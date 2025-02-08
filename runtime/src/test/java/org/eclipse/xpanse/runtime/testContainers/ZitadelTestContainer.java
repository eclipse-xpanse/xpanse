package org.eclipse.xpanse.runtime.testContainers;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
public class ZitadelTestContainer {

    private static final String DOCKER_COMPOSE_PATH =
            new File("src/test/resources/docker-compose-zitadel.yml").getAbsolutePath();
    private static DockerComposeContainer<?> zitadelComposeContainer;

    static {
        if (zitadelComposeContainer == null) {
            File dockerComposeFile = new File(DOCKER_COMPOSE_PATH);
            log.info("Loading Docker Compose from: {}", dockerComposeFile.getAbsolutePath());
            if (!dockerComposeFile.exists()) {
                throw new RuntimeException(
                        "Error: Docker Compose file not found at "
                                + dockerComposeFile.getAbsolutePath());
            }

            zitadelComposeContainer =
                    new DockerComposeContainer<>(dockerComposeFile)
                            .withExposedService("zitadel", 8080, Wait.forListeningPort())
                            .withExposedService("db", 5432, Wait.forListeningPort());

            zitadelComposeContainer.start();
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        int dynamicPort = zitadelComposeContainer.getServicePort("zitadel", 8080);
        String authEndpoint = "http://localhost:" + dynamicPort;
        registry.add("authorization.server.endpoint", () -> authEndpoint);
        log.info("Zitadel started at: {}", authEndpoint);
    }
}
