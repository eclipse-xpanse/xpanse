/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import jakarta.transaction.Transactional;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.ServiceDeployerApi;
import org.eclipse.xpanse.api.ServiceRegisterApi;
import org.eclipse.xpanse.common.openapi.OpenApiUtil;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.service.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.register.register.utils.RegisteredServicesOpenApiGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for ServiceDeployerApiTest.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@ActiveProfiles("default")
@SpringBootTest(classes = {XpanseApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceDeployerApiTest {

    private static final String CLIENT_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/"
            + "openapitools/openapi-generator-cli/6.5.0/openapi-generator-cli-6.5.0.jar";
    private static final String OPENAPI_PATH = "openapi/";
    private static final Integer SERVICER_PORT = 8080;
    private static final String uuid = "e2d4de73-1518-40f7-8de1-60f184ea6e1d";
    private static final String userId = "defaultUserId";
    private static Ocl oclRegister;
    @Autowired
    private ServiceDeployerApi serviceDeployerApi;
    @Autowired
    private ServiceRegisterApi serviceRegisterApi;

    @BeforeAll
    static void init() throws Exception {
        OclLoader oclLoader = new OclLoader();
        oclRegister = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        oclRegister.setVersion("2.1");

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("TASK_ID", uuid);
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
        registerServiceEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(CLIENT_DOWNLOAD_URL,
                OPENAPI_PATH, SERVICER_PORT);
        RegisteredServicesOpenApiGenerator
                registeredServicesOpenApiUtil = new RegisteredServicesOpenApiGenerator(
                deployVariableValidator, openApiUtil
        );
        registeredServicesOpenApiUtil.createServiceApi(registerServiceEntity);
        String openApiWorkdir = openApiUtil.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir,
                registerServiceEntity.getId().toString() + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    @Disabled
    @Test
    void getDeployedServiceDetailsById() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(registeredServiceVo.getName());
        createRequest.setVersion(registeredServiceVo.getVersion());
        createRequest.setCsp(registeredServiceVo.getCsp());
        createRequest.setCategory(registeredServiceVo.getCategory());
        createRequest.setFlavor(registeredServiceVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                registeredServiceVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        UUID deployUUid = serviceDeployerApi.deploy(createRequest);
        ServiceDetailVo deployedServiceDetailsById =
                serviceDeployerApi.getDeployedServiceDetailsById(deployUUid.toString());
        log.error(deployedServiceDetailsById.toString());
        Assertions.assertNotNull(deployedServiceDetailsById);
    }

    @Disabled
    @Test
    void listMyDeployedServices() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(registeredServiceVo.getName());
        createRequest.setVersion(registeredServiceVo.getVersion());
        createRequest.setCsp(registeredServiceVo.getCsp());
        createRequest.setCategory(registeredServiceVo.getCategory());
        createRequest.setFlavor(registeredServiceVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                registeredServiceVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        serviceDeployerApi.deploy(createRequest);
        List<ServiceVo> deployedServicesByUser =
                serviceDeployerApi.listMyDeployedServices();
        log.error(deployedServicesByUser.toString());
        Assertions.assertFalse(deployedServicesByUser.isEmpty());
    }

    @Disabled
    @Test
    void deploy() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(registeredServiceVo.getName());
        createRequest.setVersion(registeredServiceVo.getVersion());
        createRequest.setCsp(registeredServiceVo.getCsp());
        createRequest.setCategory(registeredServiceVo.getCategory());
        createRequest.setFlavor(registeredServiceVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                registeredServiceVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        Assertions.assertDoesNotThrow(() -> {
            UUID deployUUid = serviceDeployerApi.deploy(createRequest);
            Assertions.assertNotNull(deployUUid);
        });
    }

    @Disabled
    @Test
    void destroy() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(registeredServiceVo.getName());
        createRequest.setVersion(registeredServiceVo.getVersion());
        createRequest.setCsp(registeredServiceVo.getCsp());
        createRequest.setCategory(registeredServiceVo.getCategory());
        createRequest.setFlavor(registeredServiceVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                registeredServiceVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        UUID deployUUid = serviceDeployerApi.deploy(createRequest);

        Response response = serviceDeployerApi.destroy(deployUUid.toString());
        Assertions.assertTrue(response.getSuccess());
    }
}
