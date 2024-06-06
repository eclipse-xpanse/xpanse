package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.eclipse.xpanse.modules.models.workflow.migrate.view.ServiceMigrationDetails;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceMigrationApiTest extends ApisTestCommon {

    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServiceMigrationApis() throws Exception {
        // prepare data
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("ServiceMigrationApiTest-1");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
        UUID serviceId = deployService(serviceTemplate);
        testServiceMigrationApisThrowsException(serviceTemplate, serviceId);
        testServiceMigrationApisWell(serviceTemplate, serviceId);
        // clear data
        deployServiceStorage.deleteDeployService(
                deployServiceStorage.findDeployServiceById(serviceId));
        unregisterServiceTemplate(serviceTemplate.getServiceTemplateId());
    }


    void testServiceMigrationApisWell(ServiceTemplateDetailVo serviceTemplate, UUID serviceId)
            throws Exception {

        testListServiceMigrationsReturnsEmptyResult();
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            DeployRequest deployRequest = getDeployRequest(serviceTemplate);
            MigrateRequest migrateRequest = new MigrateRequest();
            BeanUtils.copyProperties(deployRequest, migrateRequest);
            migrateRequest.setOriginalServiceId(serviceId);
            migrateRequest.setCustomerServiceName("newService");
            MockHttpServletResponse migrateResponse = migrateService(migrateRequest);
            UUID migrationId =
                    objectMapper.readValue(migrateResponse.getContentAsString(), UUID.class);
            assertThat(migrateResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
            Assertions.assertTrue(Objects.nonNull(migrationId));

            MockHttpServletResponse detailsResponse = getMigrationOrderDetailsById(migrationId);
            assertThat(detailsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
            ServiceMigrationDetails details = objectMapper.readValue(
                    detailsResponse.getContentAsString(), ServiceMigrationDetails.class);
            Assertions.assertNotNull(details.getNewServiceId());
            Assertions.assertNotNull(details.getOldServiceId());
            if (waitUntilExceptedState(details.getNewServiceId(),
                    ServiceDeploymentState.DEPLOY_SUCCESS) && waitUntilExceptedState(
                    details.getOldServiceId(), ServiceDeploymentState.DESTROY_SUCCESS)) {

                MockHttpServletResponse detailsResponse1 =
                        getMigrationOrderDetailsById(migrationId);
                assertThat(detailsResponse1.getStatus()).isEqualTo(HttpStatus.OK.value());
                ServiceMigrationDetails details1 = objectMapper.readValue(
                        detailsResponse1.getContentAsString(), ServiceMigrationDetails.class);
                Assertions.assertEquals(details1.getMigrationStatus(),
                        MigrationStatus.MIGRATION_COMPLETED);

                MockHttpServletResponse listResponse = listServiceMigrations();
                List<ServiceMigrationDetails> detailsList = objectMapper.readValue(
                        listResponse.getContentAsString(), new TypeReference<>() {
                        });
                assertThat(listResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
                assertThat(detailsList.size() == 1).isTrue();
                assertThat(detailsList).isEqualTo(List.of(details1));
            }
        }
    }

    void testServiceMigrationApisThrowsException(ServiceTemplateDetailVo serviceTemplate,
                                                 UUID serviceId) throws Exception {
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        MigrateRequest migrateRequest = new MigrateRequest();
        BeanUtils.copyProperties(deployRequest, migrateRequest);
        testMigrateThrowsServiceNotFoundException(migrateRequest);
        migrateRequest.setOriginalServiceId(serviceId);
        testMigrateThrowsServiceLockedException(migrateRequest);
        testGetMigrationOrderDetailsThrowsServiceMigrationNotFoundException();
        testMigrateThrowsServiceNotFoundException(migrateRequest);
        testMigrateThrowsAccessDeniedException(serviceId, serviceTemplate);
    }

    void testListServiceMigrationsReturnsEmptyResult() throws Exception {
        MockHttpServletResponse listResponse = listServiceMigrations();
        assertThat(listResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(listResponse.getContentAsString()).isEqualTo("[]");

    }

    void testMigrateThrowsServiceNotFoundException(MigrateRequest migrateRequest) throws Exception {
        migrateRequest.setOriginalServiceId(UUID.randomUUID());
        // Setup
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        List.of(String.format("Service with id %s not found.",
                                migrateRequest.getOriginalServiceId())));
        // Run the test
        final MockHttpServletResponse response = migrateService(migrateRequest);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(expectedResponse));
    }

    void testMigrateThrowsServiceLockedException(MigrateRequest migrateRequest) throws Exception {
        // Setup
        ServiceLockConfig serviceLockConfig = new ServiceLockConfig();
        serviceLockConfig.setModifyLocked(true);
        DeployServiceEntity deployService =
                deployServiceStorage.findDeployServiceById(migrateRequest.getOriginalServiceId());
        deployService.setLockConfig(serviceLockConfig);
        deployServiceStorage.storeAndFlush(deployService);

        String message = String.format("Service with id %s is locked from migration.",
                migrateRequest.getOriginalServiceId());
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_LOCKED,
                Collections.singletonList(message));
        String result = objectMapper.writeValueAsString(expectedResponse);
        // Run the test
        final MockHttpServletResponse response = migrateService(migrateRequest);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(result);
    }

    void testGetMigrationOrderDetailsThrowsServiceMigrationNotFoundException() throws Exception {

        UUID migrationOrderId = UUID.randomUUID();

        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_MIGRATION_NOT_FOUND,
                        List.of(String.format("Service migration with id %s not found.",
                                migrationOrderId)));
        // Run the test
        final MockHttpServletResponse response = getMigrationOrderDetailsById(migrationOrderId);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(expectedResponse));
    }

    void testMigrateThrowsAccessDeniedException(UUID serviceId,
                                                ServiceTemplateDetailVo serviceTemplate)
            throws Exception {

        Response expectedResponse =
                Response.errorResponse(ResultType.ACCESS_DENIED,
                        Collections.singletonList(
                                "No permissions to migrate services belonging to other users."));

        DeployServiceEntity deployService = deployServiceStorage.findDeployServiceById(serviceId);
        deployService.setUserId(null);
        deployServiceStorage.storeAndFlush(deployService);
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        MigrateRequest migrateRequest = new MigrateRequest();
        BeanUtils.copyProperties(deployRequest, migrateRequest);
        migrateRequest.setOriginalServiceId(serviceId);

        MockHttpServletResponse response = migrateService(migrateRequest);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(expectedResponse));
    }


    MockHttpServletResponse migrateService(MigrateRequest migrateRequest)
            throws Exception {
        return mockMvc.perform(post("/xpanse/services/migration")
                        .content(objectMapper.writeValueAsString(migrateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }


    MockHttpServletResponse listServiceMigrations() throws Exception {
        return mockMvc.perform(get("/xpanse/services/migrations")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse getMigrationOrderDetailsById(UUID migrationId) throws Exception {
        return mockMvc.perform(get("/xpanse/services/migration/{migrationId}", migrationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }
}
