package org.eclipse.xpanse.common.openapi;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"openapi.download-generator-client-url=https://repo1.maven.org"
        + "/maven2/org/openapitools/openapi-generator-cli/6.6.0/openapi-generator-cli-6.6.0.jar",
        "openapi.path=openapi",
        "server.port=8080"})
@ContextConfiguration(classes = {OpenApiUrlManage.class, String.class, OpenApiGeneratorJarManage.class})
class OpenApiUrlManageTest {

    @Value("${openapi.path}")
    private String openApiPath;
    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private OpenApiUrlManage openApiUrlManageTest;

    @BeforeEach
    void setUp() {
        openApiUrlManageTest = new OpenApiUrlManage(openApiPath, serverPort);
    }

    @Test
    void testGetServiceUrl() throws IOException {
        // SetUp
        String host = InetAddress.getLocalHost().getHostAddress();
        String serviceUrl = "http://" + host + ":" + serverPort;
        // Run the test
        String result = openApiUrlManageTest.getServiceUrl();
        // Verify the results
        Assertions.assertEquals(serviceUrl, result);
    }

    @Test
    void testGetOpenApiUrl() {
        // SetUp
        String id = UUID.randomUUID().toString();
        String openApiUrl =
                openApiUrlManageTest.getServiceUrl() + "/" + openApiPath + "/" + id + ".html";
        // Run the test
        String result = openApiUrlManageTest.getOpenApiUrl(id);
        // Verify the results
        Assertions.assertEquals(openApiUrl, result);
    }
}