package org.eclipse.xpanse.common.openapi;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"openapi.download-generator-client-url=https://repo1.maven.org"
        + "/maven2/org/openapitools/openapi-generator-cli/6.5.0/openapi-generator-cli-6.5.0.jar",
        "openapi.path=test_openapi",
        "server.port=8080"})
@ContextConfiguration(classes = {OpenApiUtil.class, String.class})
class OpenApiUtilTest {

    @Value("${openapi.download-generator-client-url}")
    private String clientDownloadUrl;
    @Value("${openapi.path}")
    private String openApiPath;
    @Value("${server.port}")
    private Integer serverPort;

    @InjectMocks
    private OpenApiUtil openApiUtilTest;

    @BeforeEach
    void setUp() {
        openApiUtilTest = new OpenApiUtil(clientDownloadUrl, openApiPath, serverPort);
    }

    @Test
    void testGetServiceUrl() throws IOException {
        // SetUp
        String host = InetAddress.getLocalHost().getHostAddress();
        String serviceUrl = "http://" + host + ":" + serverPort;
        // Run the test
        String result = openApiUtilTest.getServiceUrl();
        // Verify the results
        Assertions.assertEquals(serviceUrl, result);
    }

    @Test
    void testGetOpenApiUrl() {
        // SetUp
        String id = "123";
        String openApiUrl =
                openApiUtilTest.getServiceUrl() + "/" + openApiPath + "/" + id + ".html";
        // Run the test
        String result = openApiUtilTest.getOpenApiUrl("123");
        // Verify the results
        Assertions.assertEquals(openApiUrl, result);
    }

    @Test
    void testDownloadClientJar() throws IOException {
        // SetUp
        File jarFile = new File(openApiPath + "/openapi-generator-cli-6.5.0.jar");
        Assertions.assertFalse(jarFile.exists());
        // Run the test
        boolean result = openApiUtilTest.downloadClientJar(openApiPath);
        // Verify the results
        Assertions.assertTrue(result);
    }

    @Test
    void testGetClientDownLoadUrl() {
        // Run the test
        String result = openApiUtilTest.getClientDownLoadUrl();
        // Verify the results
        Assertions.assertEquals(clientDownloadUrl, result);
    }

    @Test
    void testGetOpenapiPath() {
        // Run the test
        String result = openApiUtilTest.getOpenapiPath();
        // Verify the results
        Assertions.assertEquals(openApiPath, result);
    }
}