package org.eclipse.xpanse.runtime.testContainers;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;

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
                            .withExposedService("zitadel", 8088)
                            .withExposedService("db", 5432);

            zitadelComposeContainer.start();

            System.setProperty(
                    "zitadel.url",
                    "http://localhost:" + zitadelComposeContainer.getServicePort("zitadel", 8088));

            log.info("Zitadel started at: {}", System.getProperty("zitadel.url"));
        }
    }

    @BeforeAll
    public static void setup() {}
}
