package org.eclipse.xpanse.runtime.testContainers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ZitadelTestContainer {

    private static final String ZITADEL_IMAGE = "ghcr.io/zitadel/zitadel:latest";
    private static GenericContainer<?> zitadelContainer;

    @BeforeAll
    public static void startContainer() {
        if (zitadelContainer == null) {
            zitadelContainer =
                    new GenericContainer<>(ZITADEL_IMAGE)
                            .withExposedPorts(8080)
                            .waitingFor(Wait.forHttp("/healthz").forStatusCode(200));

            zitadelContainer.start();

            System.setProperty(
                    "zitadel.url", "http://localhost:" + zitadelContainer.getMappedPort(8080));
            System.out.println("Zitadel on: " + System.getProperty("zitadel.url"));
        }
    }

    @AfterAll
    public static void stopContainer() {
        if (zitadelContainer != null) {
            zitadelContainer.stop();
        }
    }
}
