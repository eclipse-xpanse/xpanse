package org.eclipse.xpanse.runtime.modules.common.openapi;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {OpenApiUrlManage.class, OpenApiGeneratorJarManage.class})
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=test,dev"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OpenApiCommonTest {

    @Value("${openapi.path}")
    private String openApiPath;

    @Value("${openapi.generator.client.download-url}")
    private String clientDownloadUrl;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired private OpenApiGeneratorJarManage openApiGeneratorJarManage;
    @Autowired private OpenApiUrlManage openApiUrlManage;

    @BeforeEach
    void setUp() {
        openApiUrlManage = new OpenApiUrlManage(openApiPath, serverPort);
        openApiGeneratorJarManage = new OpenApiGeneratorJarManage(clientDownloadUrl, openApiPath);
    }

    @Test
    void testGetOpenApiWorkdir() {
        String result = openApiGeneratorJarManage.getOpenApiWorkdir();
        assertNotEquals(0, result.length());
    }

    @Test
    void testGetClientDownLoadUrl() {
        // Run the test
        String result = openApiGeneratorJarManage.getClientDownLoadUrl();
        // Verify the results
        Assertions.assertEquals(clientDownloadUrl, result);
    }

    @Test
    void testGetOpenapiPath() {
        // Run the test
        String result = openApiGeneratorJarManage.getOpenapiPath();
        // Verify the results
        Assertions.assertEquals(openApiPath, result);
    }

    @Test
    @Order(1)
    void testDownloadClientJar() throws IOException {
        // SetUp
        File jarFile = openApiGeneratorJarManage.getCliFile();
        URL url = URI.create(clientDownloadUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if (!jarFile.exists()) {
            // Run the test
            boolean result = openApiGeneratorJarManage.downloadClientJar();
            // Verify the results
            Assertions.assertTrue(result);
        } else if (jarFile.delete()) {
            // Run the test
            boolean result = openApiGeneratorJarManage.downloadClientJar();
            // Verify the results
            Assertions.assertTrue(result);
        }
    }

    @Test
    @Order(2)
    void testGetServiceUrl() {
        // SetUp
        // Run the test
        String result = openApiUrlManage.getServiceUrl();
        // Verify the results
        Assertions.assertEquals("http://localhost", result);
    }

    @Test
    void testGetOpenApiUrl() {
        // SetUp
        String id = UUID.randomUUID().toString();
        String openApiUrl = openApiUrlManage.getServiceUrl() + "/" + openApiPath + id + ".html";
        // Run the test
        String result = openApiUrlManage.getOpenApiUrl(id);
        // Verify the results
        Assertions.assertEquals(openApiUrl, result);
    }
}
