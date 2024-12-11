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
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql,test",
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
        ServiceTemplateRequestInfo registerChangeInfo = serviceTemplateApi.register(ocl);
        // Verify the results
        assertNotNull(registerChangeInfo.getServiceTemplateId());
        assertNotNull(registerChangeInfo.getRequestId());
        assertFalse(registerChangeInfo.isRequestSubmittedForReview());
        UUID serviceTemplateId = registerChangeInfo.getServiceTemplateId();
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
        assertFalse(serviceTemplateDetailVo.getIsUpdatePending());
        assertTrue(serviceTemplateDetailVo.getAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> registeredHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId,
                        ServiceTemplateRequestType.REGISTER,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(registeredHistoryVos.size(), 1);
        assertEquals(
                registeredHistoryVos.getFirst().getRequestId(), registerChangeInfo.getRequestId());

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
        // Run the update test with 'isRemoveServiceTemplateUntilApproved' is true
        boolean isRemoveServiceTemplateUntilApproved = true;
        ServiceTemplateRequestInfo updateChangeInfo =
                serviceTemplateApi.update(
                        serviceTemplateId, isRemoveServiceTemplateUntilApproved, ocl);
        // Verify the results
        assertEquals(serviceTemplateId, updateChangeInfo.getServiceTemplateId());
        assertFalse(updateChangeInfo.isRequestSubmittedForReview());
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertFalse(updatedServiceTemplateDetailVo.getIsUpdatePending());
        assertTrue(updatedServiceTemplateDetailVo.getAvailableInCatalog());
        assertEquals(
                ServiceTemplateRegistrationState.APPROVED,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> updateHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId,
                        ServiceTemplateRequestType.UPDATE,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(updateHistoryVos.size(), 1);
        assertEquals(updateHistoryVos.getFirst().getRequestId(), updateChangeInfo.getRequestId());

        // Setup unregister request
        // Run the test
        ServiceTemplateRequestInfo unregisterRequest =
                serviceTemplateApi.unregister(serviceTemplateId);
        // Verify the results
        assertEquals(serviceTemplateId, unregisterRequest.getServiceTemplateId());
        assertFalse(unregisterRequest.isRequestSubmittedForReview());
        ServiceTemplateDetailVo unregisteredServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertFalse(unregisteredServiceTemplateDetailVo.getAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> unregisterHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId,
                        ServiceTemplateRequestType.UNREGISTER,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(unregisterHistoryVos.size(), 1);
        assertEquals(
                unregisterHistoryVos.getFirst().getRequestId(), unregisterRequest.getRequestId());

        // Setup reRegister request
        // Run the test
        ServiceTemplateRequestInfo reRegisterRequest =
                serviceTemplateApi.reRegisterServiceTemplate(serviceTemplateId);
        // Verify the results
        assertEquals(serviceTemplateId, reRegisterRequest.getServiceTemplateId());
        assertFalse(reRegisterRequest.isRequestSubmittedForReview());
        ServiceTemplateDetailVo reRegisteredServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertTrue(reRegisteredServiceTemplateDetailVo.getAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateRequestHistory> reRegisterHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId,
                        ServiceTemplateRequestType.RE_REGISTER,
                        ServiceTemplateRequestStatus.ACCEPTED);
        assertEquals(reRegisterHistoryVos.size(), 1);
        assertEquals(
                reRegisterHistoryVos.getFirst().getRequestId(), reRegisterRequest.getRequestId());

        // Setup delete request
        serviceTemplateApi.unregister(serviceTemplateId);
        // Run the test
        assertDoesNotThrow(() -> serviceTemplateApi.deleteServiceTemplate(serviceTemplateId));

        assertThrows(
                ServiceTemplateNotRegistered.class,
                () -> serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId));

        assertThrows(
                ServiceTemplateNotRegistered.class,
                () ->
                        serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                                serviceTemplateId, null, null));
    }
}
