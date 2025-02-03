/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestHistory;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semver4j.Semver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql,test,dev",
            "huaweicloud.auto.approve.service.template.enabled=true"
        })
@AutoConfigureMockMvc
class RegistrationWithMysqlTest extends AbstractMysqlIntegrationTest {

    @Autowired private ServiceTemplateApi serviceTemplateApi;

    @Autowired private OclLoader oclLoader;

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testServiceTemplateApisWorkWell() throws Exception {
        // Setup register request
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        // Run the test
        ServiceTemplateRequestInfo registerRequestInfo = serviceTemplateApi.register(ocl);
        // Verify the results
        assertNotNull(registerRequestInfo.getServiceTemplateId());
        assertNotNull(registerRequestInfo.getRequestId());
        assertFalse(registerRequestInfo.isRequestSubmittedForReview());
        UUID serviceTemplateId = registerRequestInfo.getServiceTemplateId();
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        assertEquals(ocl.getCloudServiceProvider().getName(), serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(), serviceTemplateDetailVo.getName());
        assertEquals(
                new Semver(ocl.getServiceVersion()).getVersion(),
                serviceTemplateDetailVo.getVersion());
        assertEquals(
                ServiceTemplateRegistrationState.APPROVED,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());
        assertFalse(serviceTemplateDetailVo.getIsReviewInProgress());
        assertTrue(serviceTemplateDetailVo.getIsAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> registeredHistoryVos =
                serviceTemplateApi.getServiceTemplateRequestHistoryForIsv(
                        serviceTemplateId,
                        ServiceTemplateRequestType.REGISTER,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(1, registeredHistoryVos.size());
        assertEquals(
                registeredHistoryVos.getFirst().getRequestId(), registerRequestInfo.getRequestId());

        // Setup list request
        List<ServiceTemplateDetailVo> serviceTemplateDetailVos =
                serviceTemplateApi.getAllServiceTemplatesByIsv(
                        ocl.getCategory(),
                        serviceTemplateDetailVo.getCsp(),
                        ocl.getName(),
                        serviceTemplateDetailVo.getVersion(),
                        ocl.getServiceHostingType(),
                        ServiceTemplateRegistrationState.APPROVED,
                        true,
                        false);
        // Verify the results
        assertTrue(CollectionUtils.isNotEmpty(serviceTemplateDetailVos));
        assertEquals(serviceTemplateDetailVos.getFirst(), serviceTemplateDetailVo);

        // Setup update request
        ocl.setDescription("update-test");
        // Run the update test with 'isUnpublishUntilApproved' is true
        boolean isUnpublishUntilApproved = true;
        ServiceTemplateRequestInfo updateRequestInfo =
                serviceTemplateApi.update(serviceTemplateId, isUnpublishUntilApproved, ocl);
        // Verify the results
        assertEquals(serviceTemplateId, updateRequestInfo.getServiceTemplateId());
        assertFalse(updateRequestInfo.isRequestSubmittedForReview());
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertFalse(updatedServiceTemplateDetailVo.getIsReviewInProgress());
        assertTrue(updatedServiceTemplateDetailVo.getIsAvailableInCatalog());
        assertEquals(
                ServiceTemplateRegistrationState.APPROVED,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> updateHistoryVos =
                serviceTemplateApi.getServiceTemplateRequestHistoryForIsv(
                        serviceTemplateId,
                        ServiceTemplateRequestType.UPDATE,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(1, updateHistoryVos.size());
        assertEquals(updateHistoryVos.getFirst().getRequestId(), updateRequestInfo.getRequestId());

        // Setup unpublish request
        // Run the test
        ServiceTemplateRequestInfo unregisterRequest =
                serviceTemplateApi.unpublish(serviceTemplateId);
        // Verify the results
        assertEquals(serviceTemplateId, unregisterRequest.getServiceTemplateId());
        assertFalse(unregisterRequest.isRequestSubmittedForReview());
        ServiceTemplateDetailVo unregisteredServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertFalse(unregisteredServiceTemplateDetailVo.getIsAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> unregisterHistoryVos =
                serviceTemplateApi.getServiceTemplateRequestHistoryForIsv(
                        serviceTemplateId,
                        ServiceTemplateRequestType.UNPUBLISH,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(1, unregisterHistoryVos.size());
        assertEquals(
                unregisterHistoryVos.getFirst().getRequestId(), unregisterRequest.getRequestId());

        // Setup reRegister request
        // Run the test
        ServiceTemplateRequestInfo reRegisterRequest =
                serviceTemplateApi.republish(serviceTemplateId);
        // Verify the results
        assertEquals(serviceTemplateId, reRegisterRequest.getServiceTemplateId());
        assertFalse(reRegisterRequest.isRequestSubmittedForReview());
        ServiceTemplateDetailVo reRegisteredServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertTrue(reRegisteredServiceTemplateDetailVo.getIsAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> reRegisterHistoryVos =
                serviceTemplateApi.getServiceTemplateRequestHistoryForIsv(
                        serviceTemplateId,
                        ServiceTemplateRequestType.REPUBLISH,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(reRegisterHistoryVos.size(), 1);
        assertEquals(
                reRegisterHistoryVos.getFirst().getRequestId(), reRegisterRequest.getRequestId());

        // Setup delete request
        serviceTemplateApi.unpublish(serviceTemplateId);
        // Run the test
        assertDoesNotThrow(() -> serviceTemplateApi.deleteServiceTemplate(serviceTemplateId));

        assertThrows(
                ServiceTemplateNotRegistered.class,
                () -> serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId));

        assertThrows(
                ServiceTemplateNotRegistered.class,
                () ->
                        serviceTemplateApi.getServiceTemplateRequestHistoryForIsv(
                                serviceTemplateId, null, null));
    }
}
