package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServiceRecreateApiTest extends ApisTestCommon {

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServiceRecreateApis() throws Exception {
        // prepare data
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ServiceTemplateDetailVo serviceTemplate =
                registerServiceTemplateAndApproveRegistration(ocl);
        addCredentialForHuaweiCloud();
        testServiceRecreateApisWell(serviceTemplate);
        testServiceRecreateApisThrowsException(serviceTemplate);
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testServiceRecreateApisWell(ServiceTemplateDetailVo serviceTemplate) throws Exception {
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        assertThat(waitServiceDeploymentIsCompleted(serviceId)).isTrue();
        MockHttpServletResponse recreateResponse = recreateService(serviceId);
        ServiceOrder servicePortingOrder =
                objectMapper.readValue(recreateResponse.getContentAsString(), ServiceOrder.class);
        assertThat(recreateResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(servicePortingOrder).isNotNull();

        assertThat(waitServiceOrderIsCompleted(servicePortingOrder.getOrderId())).isTrue();
        ServiceOrderEntity orderEntity =
                serviceOrderStorage.getEntityById(servicePortingOrder.getOrderId());
        assertThat(orderEntity.getTaskStatus()).isEqualTo(TaskStatus.SUCCESSFUL);

        ServiceOrderEntity query = new ServiceOrderEntity();
        query.setWorkflowId(orderEntity.getWorkflowId());
        List<ServiceOrderEntity> orderEntities = serviceOrderStorage.queryEntities(query);
        assertThat(orderEntities).hasSize(3);
        for (ServiceOrderEntity entity : orderEntities) {
            deleteServiceDeployment(entity.getServiceDeploymentEntity().getId());
        }
    }

    void testServiceRecreateApisThrowsException(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        assertThat(waitServiceDeploymentIsCompleted(serviceId)).isTrue();
        testRecreateThrowsServiceNotFoundException();
        testRecreateThrowsServiceLockedException(serviceId);
        testRecreateThrowsServiceNotFoundException();
        testRecreateThrowsAccessDeniedException(serviceId);
    }

    void testRecreateThrowsServiceNotFoundException() throws Exception {
        UUID serviceId = UUID.randomUUID();
        // Setup
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        List.of(String.format("Service with id %s not found.", serviceId)));
        // Run the test
        final MockHttpServletResponse response = recreateService(serviceId);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(expectedErrorResponse));
    }

    void testRecreateThrowsServiceLockedException(UUID serviceId) throws Exception {
        // Setup
        ServiceLockConfig serviceLockConfig = new ServiceLockConfig();
        serviceLockConfig.setModifyLocked(true);
        ServiceDeploymentEntity deployService =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        deployService.setLockConfig(serviceLockConfig);
        serviceDeploymentStorage.storeAndFlush(deployService);

        String message = String.format("Service with id %s is locked from recreate.", serviceId);
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_LOCKED, Collections.singletonList(message));
        String result = objectMapper.writeValueAsString(expectedErrorResponse);
        // Run the test
        final MockHttpServletResponse response = recreateService(serviceId);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(result);
    }

    void testRecreateThrowsAccessDeniedException(UUID serviceId) throws Exception {

        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED,
                        Collections.singletonList(
                                "No permission to recreate the service owned by other users."));

        ServiceDeploymentEntity deployService =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        deployService.setUserId(null);
        serviceDeploymentStorage.storeAndFlush(deployService);
        MockHttpServletResponse response = recreateService(serviceId);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(expectedErrorResponse));
    }

    MockHttpServletResponse recreateService(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/recreate/{serviceId}", serviceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
