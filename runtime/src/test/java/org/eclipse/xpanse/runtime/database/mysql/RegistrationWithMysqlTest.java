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
import org.eclipse.xpanse.modules.models.servicetemplate.change.ServiceTemplateChangeInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.change.ServiceTemplateHistoryVo;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateChangeStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateRequestType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
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
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql,test",
        "huaweicloud.auto.approve.service.template.enabled=true"})
@AutoConfigureMockMvc
class RegistrationWithMysqlTest extends AbstractMysqlIntegrationTest {

    @Autowired
    private ServiceTemplateApi serviceTemplateApi;

    @Autowired
    private OclLoader oclLoader;

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testServiceTemplateApisWorkWell() throws Exception {
        // Setup register request
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        // Run the test
        ServiceTemplateChangeInfo registerChangeInfo = serviceTemplateApi.register(ocl);
        // Verify the results
        assertNotNull(registerChangeInfo.getServiceTemplateId());
        assertNotNull(registerChangeInfo.getChangeId());
        String serviceTemplateId = registerChangeInfo.getServiceTemplateId().toString();
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        assertEquals(ocl.getCloudServiceProvider().getName(), serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(), serviceTemplateDetailVo.getName());
        assertEquals(new Semver(ocl.getServiceVersion()).getVersion(),
                serviceTemplateDetailVo.getVersion());
        assertEquals(ServiceTemplateRegistrationState.APPROVED,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());
        assertFalse(serviceTemplateDetailVo.getIsUpdatePending());
        assertTrue(serviceTemplateDetailVo.getAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateHistoryVo> registeredHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId, ServiceTemplateRequestType.REGISTER,
                        ServiceTemplateChangeStatus.ACCEPTED);
        assertEquals(registeredHistoryVos.size(), 1);
        assertEquals(registeredHistoryVos.getFirst().getChangeId(),
                registerChangeInfo.getChangeId());

        // Setup list request
        List<ServiceTemplateDetailVo> serviceTemplateDetailVos =
                serviceTemplateApi.getAllServiceTemplatesByIsv(ocl.getCategory(),
                        serviceTemplateDetailVo.getCsp(), ocl.getName(),
                        serviceTemplateDetailVo.getVersion(), ocl.getServiceHostingType(),
                        ServiceTemplateRegistrationState.APPROVED, true, false);
        // Verify the results
        assertTrue(CollectionUtils.isNotEmpty(serviceTemplateDetailVos));
        assertEquals(serviceTemplateDetailVos.getFirst(), serviceTemplateDetailVo);

        // Setup update request
        ocl.setDescription("update-test");
        // Run the update test with 'isRemoveServiceTemplateUntilApproved' is true
        boolean isRemoveServiceTemplateUntilApproved = true;
        ServiceTemplateChangeInfo updateChangeInfo = serviceTemplateApi.
                update(serviceTemplateId, isRemoveServiceTemplateUntilApproved, ocl);
        // Verify the results
        assertEquals(serviceTemplateId, updateChangeInfo.getServiceTemplateId().toString());
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertFalse(updatedServiceTemplateDetailVo.getIsUpdatePending());
        assertTrue(updatedServiceTemplateDetailVo.getAvailableInCatalog());
        assertEquals(ServiceTemplateRegistrationState.APPROVED,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());

        // Run listChangeHistory request
        List<ServiceTemplateHistoryVo> updateHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId, ServiceTemplateRequestType.UPDATE,
                        ServiceTemplateChangeStatus.ACCEPTED);
        assertEquals(updateHistoryVos.size(), 1);
        assertEquals(updateHistoryVos.getFirst().getChangeId(),
                updateChangeInfo.getChangeId());


        // Setup unregister request
        // Run the test
        ServiceTemplateChangeInfo unregisterRequest =
                serviceTemplateApi.unregister(serviceTemplateId);
        // Verify the results
        assertEquals(serviceTemplateId, unregisterRequest.getServiceTemplateId().toString());
        ServiceTemplateDetailVo unregisteredServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertFalse(unregisteredServiceTemplateDetailVo.getAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateHistoryVo> unregisterHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId, ServiceTemplateRequestType.UNREGISTER,
                        ServiceTemplateChangeStatus.ACCEPTED);
        assertEquals(unregisterHistoryVos.size(), 1);
        assertEquals(unregisterHistoryVos.getFirst().getChangeId(),
                unregisterRequest.getChangeId());

        // Setup reRegister request
        // Run the test
        ServiceTemplateChangeInfo reRegisterRequest =
                serviceTemplateApi.reRegisterServiceTemplate(serviceTemplateId);
        // Verify the results
        assertEquals(serviceTemplateId, reRegisterRequest.getServiceTemplateId().toString());
        ServiceTemplateDetailVo reRegisteredServiceTemplateDetailVo =
                serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId);
        assertTrue(reRegisteredServiceTemplateDetailVo.getAvailableInCatalog());

        // Run listChangeHistory request
        List<ServiceTemplateHistoryVo> reRegisterHistoryVos =
                serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId, ServiceTemplateRequestType.RE_REGISTER,
                        ServiceTemplateChangeStatus.ACCEPTED);
        assertEquals(reRegisterHistoryVos.size(), 1);
        assertEquals(reRegisterHistoryVos.getFirst().getChangeId(),
                reRegisterRequest.getChangeId());

        // Setup delete request
        serviceTemplateApi.unregister(serviceTemplateId);
        // Run the test
        assertDoesNotThrow(() -> serviceTemplateApi.deleteServiceTemplate(serviceTemplateId));

        assertThrows(ServiceTemplateNotRegistered.class,
                () -> serviceTemplateApi.getServiceTemplateDetailsById(serviceTemplateId));

        assertThrows(ServiceTemplateNotRegistered.class,
                () -> serviceTemplateApi.getServiceTemplateHistoryByServiceTemplateId(
                        serviceTemplateId, null, null));
    }

}
