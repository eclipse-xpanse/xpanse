package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
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
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test"})
@AutoConfigureMockMvc
class ServiceMigrationApiTest extends ApisTestCommon {

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServiceMigrationApis() throws Exception {
        // prepare data
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
        addCredentialForHuaweiCloud();
        testServiceMigrationApisWell(serviceTemplate);
        testServiceMigrationApisThrowsException(serviceTemplate);
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }


    void testServiceMigrationApisWell(ServiceTemplateDetailVo serviceTemplate) throws Exception {
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        assertThat(waitServiceDeploymentIsCompleted(serviceId)).isTrue();
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        MigrateRequest migrateRequest = new MigrateRequest();
        BeanUtils.copyProperties(deployRequest, migrateRequest);
        migrateRequest.setOriginalServiceId(serviceId);
        migrateRequest.setCustomerServiceName("newService");
        MockHttpServletResponse migrateResponse = migrateService(migrateRequest);
        ServiceOrder migrationOrder = objectMapper.readValue(migrateResponse.getContentAsString(),
                ServiceOrder.class);
        assertThat(migrateResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(migrationOrder).isNotNull();

        assertThat(waitServiceOrderIsCompleted(migrationOrder.getOrderId())).isTrue();
        ServiceOrderEntity orderEntity =
                serviceOrderStorage.getEntityById(migrationOrder.getOrderId());
        assertThat(orderEntity.getTaskStatus()).isEqualTo(TaskStatus.SUCCESSFUL);

        ServiceOrderEntity query = new ServiceOrderEntity();
        query.setWorkflowId(orderEntity.getWorkflowId());
        List<ServiceOrderEntity> orderEntities = serviceOrderStorage.queryEntities(query);
        assertThat(orderEntities).hasSize(3);
        for (ServiceOrderEntity entity : orderEntities) {
            deleteServiceDeployment(entity.getServiceDeploymentEntity().getId());
        }
    }


    void testServiceMigrationApisThrowsException(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        assertThat(waitServiceDeploymentIsCompleted(serviceId)).isTrue();
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        MigrateRequest migrateRequest = new MigrateRequest();
        BeanUtils.copyProperties(deployRequest, migrateRequest);
        testMigrateThrowsServiceNotFoundException(migrateRequest);
        migrateRequest.setOriginalServiceId(serviceId);
        testMigrateThrowsServiceLockedException(migrateRequest);
        testMigrateThrowsServiceNotFoundException(migrateRequest);
        testMigrateThrowsAccessDeniedException(serviceId, serviceTemplate);
    }


    void testMigrateThrowsServiceNotFoundException(MigrateRequest migrateRequest) throws Exception {
        migrateRequest.setOriginalServiceId(UUID.randomUUID());
        // Setup
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
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
        ServiceDeploymentEntity deployService = serviceDeploymentStorage
                .findServiceDeploymentById(migrateRequest.getOriginalServiceId());
        deployService.setLockConfig(serviceLockConfig);
        serviceDeploymentStorage.storeAndFlush(deployService);

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


    void testMigrateThrowsAccessDeniedException(UUID serviceId,
                                                ServiceTemplateDetailVo serviceTemplate)
            throws Exception {

        Response expectedResponse = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList(
                        "No permissions to migrate services belonging to other users."));

        ServiceDeploymentEntity deployService =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        deployService.setUserId(null);
        serviceDeploymentStorage.storeAndFlush(deployService);
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


    MockHttpServletResponse migrateService(MigrateRequest migrateRequest) throws Exception {
        return mockMvc.perform(post("/xpanse/services/migration").content(
                                objectMapper.writeValueAsString(migrateRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }
}
