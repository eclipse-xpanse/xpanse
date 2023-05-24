/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.register.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.register.RegisterServiceStorage;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.TerraformDeployment;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.eclipse.xpanse.modules.models.view.CategoryOclVo;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.eclipse.xpanse.orchestrator.utils.OpenApiUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for RegisterServiceImpl.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class RegisterServiceImplTest {

    private static final String oclLocation = "file:src/test/resources/ocl_test.yaml";
    private static OclLoader oclLoader;
    private static Ocl oclRegister;
    private static UUID uuid;

    @Mock
    private RegisterServiceStorage mockStorage;
    @Mock
    private OclLoader mockOclLoader;
    @Mock
    private OpenApiUtil mockOpenApiUtil;
    @Mock
    private OrchestratorService mockOrchestratorService;
    @InjectMocks
    private RegisterServiceImpl registerServiceImplUnderTest;

    @BeforeAll
    static void init() throws Exception {
        oclLoader = new OclLoader();
        oclRegister = oclLoader.getOcl(new URL(oclLocation));
        uuid = UUID.fromString("ed6248d4-2bcd-4e94-84b0-29e014c05137");
    }

    @Test
    void testUpdateRegisteredServiceByUrl() throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        ocl.setVersion("2.1");

        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setName(oclRegister.getName());
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setCategory(oclRegister.getCategory());
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        registerServiceEntity.setVersion(oclRegister.getServiceVersion());
        registerServiceEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        registerServiceEntity.setOcl(oclRegister);

        when(mockOclLoader.getOcl(new URL(oclLocation))).thenReturn(ocl);
        when(mockStorage.getRegisterServiceById(uuid)).thenReturn(registerServiceEntity);
        TerraformDeployment deployment = new TerraformDeployment("test", true, "DEBUG");
        doReturn(deployment).when(mockOrchestratorService).getDeployment(any());

        RegisterServiceEntity registeredServiceEntityByUrl =
                registerServiceImplUnderTest.updateRegisteredServiceByUrl(uuid.toString(),
                        oclLocation);
        log.error(registeredServiceEntityByUrl.toString());
        Assertions.assertEquals(ServiceState.UPDATED,
                registeredServiceEntityByUrl.getServiceState());
        verify(mockOpenApiUtil).updateServiceApi(registerServiceEntity);
    }

    @Test
    void testUpdateRegisteredServiceByUrl_OclLoaderThrowsException() throws Exception {
        when(mockOclLoader.getOcl(new URL(oclLocation))).thenThrow(Exception.class);
        assertThatThrownBy(() -> registerServiceImplUnderTest.updateRegisteredServiceByUrl(
                UUID.randomUUID().toString(), oclLocation))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testUpdateRegisteredService() throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        ocl.setVersion("2.1");
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setName(oclRegister.getName());
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setCategory(oclRegister.getCategory());
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        registerServiceEntity.setVersion(oclRegister.getServiceVersion());
        registerServiceEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        registerServiceEntity.setOcl(oclRegister);

        when(mockStorage.getRegisterServiceById(uuid)).thenReturn(registerServiceEntity);
        TerraformDeployment deployment = new TerraformDeployment("test", true, "DEBUG");
        doReturn(deployment).when(mockOrchestratorService).getDeployment(any());

        RegisterServiceEntity updateRegisteredServiceEntity =
                registerServiceImplUnderTest.updateRegisteredService(uuid.toString(), ocl);
        log.error(updateRegisteredServiceEntity.toString());
        Assertions.assertEquals(ServiceState.UPDATED,
                updateRegisteredServiceEntity.getServiceState());
        verify(mockOpenApiUtil).updateServiceApi(registerServiceEntity);
    }

    @Test
    void testRegisterService() {
        TerraformDeployment deployment = new TerraformDeployment("test", true, "DEBUG");
        doReturn(deployment).when(mockOrchestratorService).getDeployment(any());
        RegisterServiceEntity registerServiceEntity =
                registerServiceImplUnderTest.registerService(oclRegister);
        Assertions.assertEquals(ServiceState.REGISTERED, registerServiceEntity.getServiceState());
        verify(mockOpenApiUtil).generateServiceApi(registerServiceEntity);
    }

    @Test
    void testRegisterServiceByUrl() throws Exception {
        when(mockOclLoader.getOcl(new URL(oclLocation))).thenReturn(oclRegister);
        TerraformDeployment deployment = new TerraformDeployment("test", true, "DEBUG");
        doReturn(deployment).when(mockOrchestratorService).getDeployment(any());
        RegisterServiceEntity registerServiceEntity =
                registerServiceImplUnderTest.registerServiceByUrl(oclLocation);
        Assertions.assertEquals(ServiceState.REGISTERED, registerServiceEntity.getServiceState());
        verify(mockOpenApiUtil).generateServiceApi(registerServiceEntity);
    }

    @Test
    void testRegisterServiceByUrl_OclLoaderThrowsException() throws Exception {
        when(mockOclLoader.getOcl(new URL(oclLocation))).thenThrow(Exception.class);
        assertThatThrownBy(() -> registerServiceImplUnderTest.registerServiceByUrl(
                oclLocation)).isInstanceOf(Exception.class);
    }

    @Test
    void testGetRegisteredService() {
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setName(oclRegister.getName());
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setCategory(oclRegister.getCategory());
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        registerServiceEntity.setVersion(oclRegister.getServiceVersion());
        registerServiceEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        registerServiceEntity.setOcl(oclRegister);

        when(mockStorage.getRegisterServiceById(uuid)).thenReturn(registerServiceEntity);
        RegisterServiceEntity registeredService =
                registerServiceImplUnderTest.getRegisteredService(uuid.toString());
        Assertions.assertEquals(registerServiceEntity, registeredService);
    }

    @Test
    void testQueryRegisteredServices() {
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setName(oclRegister.getName());
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setCategory(oclRegister.getCategory());
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        registerServiceEntity.setVersion(oclRegister.getServiceVersion());
        registerServiceEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        registerServiceEntity.setOcl(oclRegister);
        List<RegisterServiceEntity> registerServiceEntities = new ArrayList<>();
        registerServiceEntities.add(registerServiceEntity);
        when(mockStorage.queryRegisteredServices(new RegisteredServiceQuery())).thenReturn(
                registerServiceEntities);

        List<RegisterServiceEntity> registerServiceEntities1 =
                registerServiceImplUnderTest.queryRegisteredServices(new RegisteredServiceQuery());
        Assertions.assertEquals(registerServiceEntities, registerServiceEntities1);
    }

    @Test
    void testQueryRegisteredServices_RegisterServiceStorageReturnsNoItems() {
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        query.setCsp(Csp.HUAWEI);
        query.setCategory(Category.MIDDLEWARE);
        query.setServiceName("kafka");
        query.setServiceVersion("v3.1.1");

        RegisteredServiceQuery query1 = new RegisteredServiceQuery();
        query1.setCsp(Csp.HUAWEI);
        query1.setCategory(Category.MIDDLEWARE);
        query1.setServiceName("kafka");
        query1.setServiceVersion("v3.1.1");
        when(mockStorage.queryRegisteredServices(query1)).thenReturn(Collections.emptyList());

        List<RegisterServiceEntity> result =
                registerServiceImplUnderTest.queryRegisteredServices(query);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetManagedServicesTree() {
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setName(oclRegister.getName());
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setCategory(oclRegister.getCategory());
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        registerServiceEntity.setVersion(oclRegister.getServiceVersion());
        registerServiceEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        registerServiceEntity.setOcl(oclRegister);
        List<RegisterServiceEntity> registerServiceEntities = new ArrayList<>();
        registerServiceEntities.add(registerServiceEntity);
        when(mockStorage.queryRegisteredServices(new RegisteredServiceQuery())).thenReturn(
                registerServiceEntities);

        List<CategoryOclVo> managedServicesTree =
                registerServiceImplUnderTest.getManagedServicesTree(new RegisteredServiceQuery());
        Assertions.assertNotNull(managedServicesTree);
        log.error(managedServicesTree.toString());
    }

    @Test
    void testGetManagedServicesTree_RegisterServiceStorageReturnsNoItems() {
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        query.setCsp(Csp.HUAWEI);
        query.setCategory(Category.MIDDLEWARE);
        query.setServiceName("kafka");
        query.setServiceVersion("v3.1.1");

        RegisteredServiceQuery query1 = new RegisteredServiceQuery();
        query1.setCsp(Csp.HUAWEI);
        query1.setCategory(Category.MIDDLEWARE);
        query1.setServiceName("kafka");
        query1.setServiceVersion("v3.1.1");
        when(mockStorage.queryRegisteredServices(query1)).thenReturn(Collections.emptyList());

        List<CategoryOclVo> result =
                registerServiceImplUnderTest.getManagedServicesTree(query);
        Assertions.assertEquals(result, Collections.emptyList());
    }

    @Test
    void testUnregisterService() {
        registerServiceImplUnderTest.unregisterService(uuid.toString());
        verify(mockStorage).removeById(uuid);
        verify(mockOpenApiUtil).deleteServiceApi(uuid.toString());
    }

    @Test
    void testGetOpenApiUrl() {
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setName(oclRegister.getName());
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setCategory(oclRegister.getCategory());
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        registerServiceEntity.setVersion(oclRegister.getServiceVersion());
        registerServiceEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        registerServiceEntity.setOcl(oclRegister);

        when(mockStorage.getRegisterServiceById(uuid))
                .thenReturn(registerServiceEntity);
        when(mockOpenApiUtil.getOpenApi(registerServiceEntity)).thenReturn("result");
        String result =
                registerServiceImplUnderTest.getOpenApiUrl(uuid.toString());
        Assertions.assertEquals(result, "result");
    }
}