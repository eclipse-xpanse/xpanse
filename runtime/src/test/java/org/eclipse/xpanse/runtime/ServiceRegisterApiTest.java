/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import jakarta.transaction.Transactional;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.openapi.OpenApiUtil;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.admin.SystemStatus;
import org.eclipse.xpanse.modules.models.admin.enums.HealthStatus;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.register.register.utils.RegisteredServicesOpenApiGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for ServiceRegisterApiTest.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {XpanseApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceRegisterApiTest {

    private static final String CLIENT_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/"
            + "openapitools/openapi-generator-cli/6.5.0/openapi-generator-cli-6.5.0.jar";
    private static final String OPENAPI_PATH = "openapi/";
    private static final Integer SERVICER_PORT = 8080;
    private static OclLoader oclLoader;
    private static Ocl oclRegister;

    @Autowired
    private org.eclipse.xpanse.api.ServiceRegisterApi serviceRegisterApi;

    @BeforeAll
    static void init() throws Exception {
        oclLoader = new OclLoader();
        oclRegister = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        oclRegister.setVersion("2.1");

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("TASK_ID", UUID.randomUUID().toString());
        MDC.setContextMap(contextMap);
    }

    @AfterAll
    static void tearDown() {
        MDC.clear();
    }

    @Test
    @Order(1)
    void testDownLoadOpenApiJar() {
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setName("kafka");
        registerServiceEntity.setVersion("2.0");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.MIDDLEWARE);
        registerServiceEntity.setOcl(oclRegister);
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(CLIENT_DOWNLOAD_URL,
                OPENAPI_PATH, SERVICER_PORT);
        RegisteredServicesOpenApiGenerator
                registeredServicesOpenApiGenerator = new RegisteredServicesOpenApiGenerator(
                deployVariableValidator, openApiUtil);
        registeredServicesOpenApiGenerator.createServiceApi(registerServiceEntity);
        String openApiWorkdir = openApiUtil.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir,
                registerServiceEntity.getId().toString() + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    @Test
    void listCategories() {
        List<Category> categories = Arrays.asList(Category.values());
        List<Category> categoryList = serviceRegisterApi.listCategories();
        Assertions.assertEquals(categories, categoryList);
    }

    @Test
    void register() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);
        log.error(registeredServiceVo.toString());
        Assertions.assertNotNull(registeredServiceVo);
    }

    @Test
    void update() throws Exception {

        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);

        Ocl oclUpdate = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));
        RegisteredServiceVo updateServiceVo =
                serviceRegisterApi.update(registeredServiceVo.getId().toString(), oclUpdate);
        Thread.sleep(3000);
        Assertions.assertEquals(registeredServiceVo.getId(), updateServiceVo.getId());
        Assertions.assertNotEquals(registeredServiceVo.getOcl(), updateServiceVo.getOcl());
    }

    @Test
    void fetch() throws Exception {
        String oclLocation = "file:./target/test-classes/ocl_test.yaml";
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.fetch(oclLocation);
        Thread.sleep(3000);
        Assertions.assertNotNull(registeredServiceVo);
    }

    @Test
    void fetchUpdate() throws Exception {

        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);

        String oclLocationUpdate = "file:./target/test-classes/ocl_test.yaml";
        RegisteredServiceVo fetchUpdateServiceVo =
                serviceRegisterApi.fetchUpdate(registeredServiceVo.getId().toString(),
                        oclLocationUpdate);
        Thread.sleep(3000);
        Assertions.assertEquals(registeredServiceVo.getId(), fetchUpdateServiceVo.getId());
        Assertions.assertNotEquals(registeredServiceVo.getOcl(), fetchUpdateServiceVo.getOcl());
    }

    @Test
    void unregister() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);
        Response response = serviceRegisterApi.unregister(registeredServiceVo.getId().toString());
        Assertions.assertTrue(response.getSuccess());
    }

    @Test
    void listRegisteredServices() throws Exception {
        serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);

        Category categoryName = oclRegister.getCategory();
        String cspName = oclRegister.getCloudServiceProvider().getName().name();
        String serviceName = oclRegister.getName();
        String serviceVersion = oclRegister.getServiceVersion();
        List<RegisteredServiceVo> registeredServiceVos = serviceRegisterApi.listRegisteredServices(
                categoryName, cspName, serviceName, serviceVersion);
        log.error(registeredServiceVos.toString());
        Assertions.assertNotNull(registeredServiceVos);
    }

    @Test
    void detail() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);

        RegisteredServiceVo registeredServiceVoDetail =
                serviceRegisterApi.detail(registeredServiceVo.getId().toString());
        log.error(registeredServiceVoDetail.toString());
        Assertions.assertNotNull(registeredServiceVoDetail);
    }

    @Test
    void health() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        SystemStatus health = serviceRegisterApi.health();
        Assertions.assertEquals(systemStatus, health);
    }
}
