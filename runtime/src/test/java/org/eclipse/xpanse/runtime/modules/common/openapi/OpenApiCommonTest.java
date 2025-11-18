package org.eclipse.xpanse.runtime.modules.common.openapi;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.eclipse.xpanse.common.config.OpenApiGeneratorProperties;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@ContextConfiguration(
        classes = {
            OpenApiUrlManage.class,
            OpenApiGeneratorJarManage.class,
            OpenApiGeneratorProperties.class
        })
@SpringBootTest(properties = {"spring.profiles.active=test,dev"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(
        properties = {
            "xpanse.openapi-generator.client.download-url=https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/7.16.0/openapi-generator-cli-7.16.0.jar"
        })
@Import(RefreshAutoConfiguration.class)
@ConfigurationPropertiesScan
class OpenApiCommonTest {

    @Autowired private OpenApiGeneratorJarManage openApiGeneratorJarManage;
    @Autowired private OpenApiUrlManage openApiUrlManage;
    @Autowired private OpenApiGeneratorProperties openApiGeneratorProperties;
    @Autowired private Environment environment;

    @BeforeEach
    void setUp() {
        openApiUrlManage = new OpenApiUrlManage(openApiGeneratorProperties, environment);
        openApiGeneratorJarManage = new OpenApiGeneratorJarManage(openApiGeneratorProperties);
    }

    @Test
    void testGetOpenApiWorkdir() {
        String result = openApiGeneratorJarManage.getOpenApiWorkdir();
        assertNotEquals(0, result.length());
    }

    @Test
    @Order(1)
    void testDownloadClientJar() throws IOException {
        // SetUp
        File jarFile = openApiGeneratorJarManage.getCliFile();
        URL url = URI.create(openApiGeneratorProperties.getClient().getDownloadUrl()).toURL();
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
        String openApiUrl =
                openApiUrlManage.getServiceUrl()
                        + "/"
                        + openApiGeneratorProperties.getFileGenerationPath()
                        + id
                        + ".html";
        // Run the test
        String result = openApiUrlManage.getOpenApiUrl(id);
        // Verify the results
        Assertions.assertEquals(openApiUrl, result);
    }
}
