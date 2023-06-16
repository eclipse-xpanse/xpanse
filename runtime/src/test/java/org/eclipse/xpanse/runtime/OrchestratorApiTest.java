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
import org.eclipse.xpanse.api.OrchestratorApi;
import org.eclipse.xpanse.api.response.Response;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.admin.SystemStatus;
import org.eclipse.xpanse.modules.models.admin.enums.HealthStatus;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.models.service.view.UserAvailableServiceVo;
import org.eclipse.xpanse.modules.plugin.utils.OpenApiUtil;
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
import org.springframework.hateoas.Link;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for OrchestratorApi.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {XpanseApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrchestratorApiTest {

    private static final String CLIENT_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/"
            + "openapitools/openapi-generator-cli/6.5.0/openapi-generator-cli-6.5.0.jar";
    private static final String OPENAPI_PATH = "openapi/";
    private static final Integer SERVICER_PORT = 8080;
    private static OclLoader oclLoader;
    private static Ocl oclRegister;

    @Autowired
    private OrchestratorApi orchestratorApi;

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
        OpenApiUtil openApiUtil = new OpenApiUtil(deployVariableValidator, CLIENT_DOWNLOAD_URL,
                OPENAPI_PATH, SERVICER_PORT);
        openApiUtil.createServiceApi(registerServiceEntity);
        String openApiWorkdir = openApiUtil.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir,
                registerServiceEntity.getId().toString() + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    @Test
    void listCategories() {
        List<Category> categories = Arrays.asList(Category.values());
        List<Category> categoryList = orchestratorApi.listCategories();
        Assertions.assertEquals(categories, categoryList);
    }

    @Test
    void register() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        log.error(registeredServiceVo.toString());
        Assertions.assertNotNull(registeredServiceVo);
    }

    @Test
    void update() throws Exception {

        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);

        Ocl oclUpdate = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));
        RegisteredServiceVo updateServiceVo =
                orchestratorApi.update(registeredServiceVo.getId().toString(), oclUpdate);
        Thread.sleep(3000);
        Assertions.assertEquals(registeredServiceVo.getId(), updateServiceVo.getId());
        Assertions.assertNotEquals(registeredServiceVo.getOcl(), updateServiceVo.getOcl());
    }

    @Test
    void fetch() throws Exception {
        String oclLocation = "file:./target/test-classes/ocl_test.yaml";
        RegisteredServiceVo registeredServiceVo = orchestratorApi.fetch(oclLocation);
        Thread.sleep(3000);
        Assertions.assertNotNull(registeredServiceVo);
    }

    @Test
    void fetchUpdate() throws Exception {

        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);

        String oclLocationUpdate = "file:./target/test-classes/ocl_test.yaml";
        RegisteredServiceVo fetchUpdateServiceVo =
                orchestratorApi.fetchUpdate(registeredServiceVo.getId().toString(),
                        oclLocationUpdate);
        Thread.sleep(3000);
        Assertions.assertEquals(registeredServiceVo.getId(), fetchUpdateServiceVo.getId());
        Assertions.assertNotEquals(registeredServiceVo.getOcl(), fetchUpdateServiceVo.getOcl());
    }

    @Test
    void unregister() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        Response response = orchestratorApi.unregister(registeredServiceVo.getId().toString());
        Assertions.assertTrue(response.getSuccess());
    }

    @Test
    void listRegisteredServices() throws Exception {
        orchestratorApi.register(oclRegister);
        Thread.sleep(3000);

        Category categoryName = oclRegister.getCategory();
        String cspName = oclRegister.getCloudServiceProvider().getName().name();
        String serviceName = oclRegister.getName();
        String serviceVersion = oclRegister.getServiceVersion();
        List<RegisteredServiceVo> registeredServiceVos = orchestratorApi.listRegisteredServices(
                categoryName, cspName, serviceName, serviceVersion);
        log.error(registeredServiceVos.toString());
        Assertions.assertNotNull(registeredServiceVos);
    }

    @Test
    void detail() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);

        RegisteredServiceVo registeredServiceVoDetail =
                orchestratorApi.detail(registeredServiceVo.getId().toString());
        log.error(registeredServiceVoDetail.toString());
        Assertions.assertNotNull(registeredServiceVoDetail);
    }

    @Test
    void health() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        SystemStatus health = orchestratorApi.health();
        Assertions.assertEquals(systemStatus, health);
    }

    @Disabled
    @Test
    void getDeployedServiceDetailsById() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserName("admin");
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

        UUID deployUUid = orchestratorApi.deploy(createRequest);
        ServiceDetailVo deployedServiceDetailsById =
                orchestratorApi.getDeployedServiceDetailsById(deployUUid.toString(),
                        createRequest.getUserName());
        log.error(deployedServiceDetailsById.toString());
        Assertions.assertNotNull(deployedServiceDetailsById);
    }

    @Disabled
    @Test
    void listDeployedServices() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserName("admin");
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

        orchestratorApi.deploy(createRequest);
        List<ServiceVo> serviceVos = orchestratorApi.listDeployedServices();
        log.error(serviceVos.toString());
        Assertions.assertFalse(serviceVos.isEmpty());
    }

    @Disabled
    @Test
    void getDeployedServicesByUser() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserName("admin");
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

        orchestratorApi.deploy(createRequest);
        List<ServiceVo> deployedServicesByUser =
                orchestratorApi.getDeployedServicesByUser(createRequest.getUserName());
        log.error(deployedServicesByUser.toString());
        Assertions.assertFalse(deployedServicesByUser.isEmpty());
    }

    @Disabled
    @Test
    void deploy() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserName("admin");
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
            UUID deployUUid = orchestratorApi.deploy(createRequest);
            Assertions.assertNotNull(deployUUid);
        });
    }

    @Disabled
    @Test
    void destroy() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserName("admin");
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

        UUID deployUUid = orchestratorApi.deploy(createRequest);

        Response response = orchestratorApi.destroy(deployUUid.toString());
        Assertions.assertTrue(response.getSuccess());
    }

    @Test
    void listAvailableServices() throws Exception {
        orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        Category categoryName = oclRegister.getCategory();
        String cspName = oclRegister.getCloudServiceProvider().getName().name();
        String serviceName = oclRegister.getName();
        String serviceVersion = oclRegister.getServiceVersion();
        List<UserAvailableServiceVo> userAvailableServiceVos =
                orchestratorApi.listAvailableServices(categoryName, cspName, serviceName,
                        serviceVersion);
        log.error(userAvailableServiceVos.toString());
        Assertions.assertNotNull(userAvailableServiceVos);
    }

    @Test
    void getAvailableServicesTree() throws Exception {
        orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        List<CategoryOclVo> categoryOclVos =
                orchestratorApi.getAvailableServicesTree(oclRegister.getCategory());
        log.error(categoryOclVos.toString());
        Assertions.assertNotNull(categoryOclVos);
    }

    @Test
    void availableServiceDetails() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        UserAvailableServiceVo userAvailableServiceVo =
                orchestratorApi.availableServiceDetails(registeredServiceVo.getId().toString());
        log.error(userAvailableServiceVo.toString());
        Assertions.assertNotNull(userAvailableServiceVo);
    }

    @Test
    void openApi() throws Exception {
        RegisteredServiceVo registeredServiceVo = orchestratorApi.register(oclRegister);
        Thread.sleep(3000);
        Link link = orchestratorApi.openApi(registeredServiceVo.getId().toString());
        log.error(link.toString());
        Assertions.assertNotNull(link);
        Assertions.assertEquals("OpenApi", link.getRel().toString());
        Assertions.assertTrue(link.getHref().contains(registeredServiceVo.getId().toString()));
    }
}
