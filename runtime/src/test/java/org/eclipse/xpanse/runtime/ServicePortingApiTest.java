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
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.workflow.serviceporting.ServicePortingRequest;
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
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServicePortingApiTest extends ApisTestCommon {

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServicePortingApis() throws Exception {
        // prepare data
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ServiceTemplateDetailVo serviceTemplate =
                registerServiceTemplateAndApproveRegistration(ocl);
        addCredentialForHuaweiCloud();
        testServicePortingApisWell(serviceTemplate);
        testServicePortingApisThrowsException(serviceTemplate);
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testServicePortingApisWell(ServiceTemplateDetailVo serviceTemplate) throws Exception {
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        assertThat(waitServiceDeploymentIsCompleted(serviceId)).isTrue();
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        ServicePortingRequest servicePortingRequest = new ServicePortingRequest();
        BeanUtils.copyProperties(deployRequest, servicePortingRequest);
        servicePortingRequest.setOriginalServiceId(serviceId);
        servicePortingRequest.setCustomerServiceName("newService");
        MockHttpServletResponse servicePortingResponse = portService(servicePortingRequest);
        ServiceOrder servicePortingOrder =
                objectMapper.readValue(
                        servicePortingResponse.getContentAsString(), ServiceOrder.class);
        assertThat(servicePortingResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
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

    void testServicePortingApisThrowsException(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        assertThat(waitServiceDeploymentIsCompleted(serviceId)).isTrue();
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        ServicePortingRequest servicePortingRequest = new ServicePortingRequest();
        BeanUtils.copyProperties(deployRequest, servicePortingRequest);
        testServicePortingThrowsServiceNotFoundException(servicePortingRequest);
        servicePortingRequest.setOriginalServiceId(serviceId);
        testServicePortingThrowsServiceLockedException(servicePortingRequest);
        testServicePortingThrowsServiceNotFoundException(servicePortingRequest);
        testServicePortingThrowsAccessDeniedException(serviceId, serviceTemplate);
    }

    void testServicePortingThrowsServiceNotFoundException(
            ServicePortingRequest servicePortingRequest) throws Exception {
        servicePortingRequest.setOriginalServiceId(UUID.randomUUID());
        // Setup
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        List.of(
                                String.format(
                                        "Service with id %s not found.",
                                        servicePortingRequest.getOriginalServiceId())));
        // Run the test
        final MockHttpServletResponse response = portService(servicePortingRequest);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(expectedErrorResponse));
    }

    void testServicePortingThrowsServiceLockedException(ServicePortingRequest servicePortingRequest)
            throws Exception {
        // Setup
        ServiceLockConfig serviceLockConfig = new ServiceLockConfig();
        serviceLockConfig.setModifyLocked(true);
        ServiceDeploymentEntity deployService =
                serviceDeploymentStorage.findServiceDeploymentById(
                        servicePortingRequest.getOriginalServiceId());
        deployService.setLockConfig(serviceLockConfig);
        serviceDeploymentStorage.storeAndFlush(deployService);

        String message =
                String.format(
                        "Service with id %s is locked from porting.",
                        servicePortingRequest.getOriginalServiceId());
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_LOCKED, Collections.singletonList(message));
        String result = objectMapper.writeValueAsString(expectedErrorResponse);
        // Run the test
        final MockHttpServletResponse response = portService(servicePortingRequest);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo(result);
    }

    void testServicePortingThrowsAccessDeniedException(
            UUID serviceId, ServiceTemplateDetailVo serviceTemplate) throws Exception {

        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED,
                        Collections.singletonList(
                                "No permission to port the service owned by other users."));

        ServiceDeploymentEntity deployService =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        deployService.setUserId(null);
        serviceDeploymentStorage.storeAndFlush(deployService);
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        ServicePortingRequest servicePortingRequest = new ServicePortingRequest();
        BeanUtils.copyProperties(deployRequest, servicePortingRequest);
        servicePortingRequest.setOriginalServiceId(serviceId);

        MockHttpServletResponse response = portService(servicePortingRequest);

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(expectedErrorResponse));
    }

    MockHttpServletResponse portService(ServicePortingRequest servicePortingRequest)
            throws Exception {
        return mockMvc.perform(
                        post("/xpanse/services/porting")
                                .content(objectMapper.writeValueAsString(servicePortingRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
