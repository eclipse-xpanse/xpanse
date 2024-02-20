/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.DeployerKindManager;
import org.eclipse.xpanse.modules.deployment.ResourceHandlerManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.callbacks.TerraformDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TerraformProviderHelper;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

/**
 * Test for ServiceTemplateImpl.
 */
@Slf4j
@ExtendWith({MockitoExtension.class})
class ServiceTemplateManageTest {

    private static final String oclLocation = "file:src/test/resources/ocl_test.yaml";
    private static OclLoader oclLoader;
    private static Ocl oclRegister;
    private static UUID uuid;
    @Mock
    TerraformLocalConfig terraformLocalConfig;
    @Mock
    Executor taskExecutor;
    @Mock
    ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;
    @Mock
    private ServiceTemplateStorage mockStorage;
    @Mock
    private OclLoader mockOclLoader;
    @Mock
    private ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator;
    @Mock
    private DeployerKindManager deployerKindManager;
    @Mock
    private IdentityProviderManager identityProviderManager;
    @Mock
    private TerraformDeploymentResultCallbackManager terraformDeploymentResultCallbackManager;
    @Mock
    private DeployServiceEntityHandler deployServiceEntityHandler;
    @InjectMocks
    private ServiceTemplateManage serviceTemplateManageTest;
    @Mock
    private ResourceHandlerManager resourceHandlerManager;
    @Mock
    private ScriptsGitRepoManage scriptsGitRepoManage;
    @Mock
    private TerraformProviderHelper terraformProviderHelper;

    @BeforeAll
    static void init() throws Exception {
        oclLoader = new OclLoader();
        oclRegister = oclLoader.getOcl(URI.create(oclLocation).toURL());
        uuid = UUID.fromString("ed6248d4-2bcd-4e94-84b0-29e014c05137");
    }

    ServiceTemplateEntity getServiceTemplateEntity() {
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setServiceHostingType(oclRegister.getServiceHostingType());
        serviceTemplateEntity.setOcl(oclRegister);
        serviceTemplateEntity.setNamespace("ISV-A");
        return serviceTemplateEntity;
    }

    @Test
    void testUpdateServiceTemplateByUrl() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ocl.setVersion("2.1");
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        when(mockOclLoader.getOcl(URI.create(oclLocation).toURL())).thenReturn(ocl);
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        when(mockStorage.storeAndFlush(any())).thenReturn(serviceTemplateEntity);
        TerraformLocalDeployment deployment =
                new TerraformLocalDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, terraformProviderHelper, taskExecutor,
                        terraformDeploymentResultCallbackManager, deployServiceEntityHandler,
                        scriptsGitRepoManage);

        doReturn(deployment).when(deployerKindManager).getDeployment(any());
        doReturn("""
                terraform {
                  required_providers {
                    huaweicloud = {
                      source = "huaweicloud/huaweicloud"
                      version = "~> 1.51.0"
                    }
                  }
                }
                            
                provider "huaweicloud" {
                  region = "test"
                }
                """).when(this.terraformProviderHelper)
                .getProvider(any(Csp.class), any());
        when(identityProviderManager.getUserNamespace()).thenReturn(Optional.of("ISV-A"));
        ServiceTemplateEntity ServiceTemplateEntityByUrl =
                serviceTemplateManageTest.updateServiceTemplateByUrl(uuid.toString(),
                        oclLocation);
        log.error(ServiceTemplateEntityByUrl.toString());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                ServiceTemplateEntityByUrl.getServiceRegistrationState());
        verify(serviceTemplateOpenApiGenerator).updateServiceApi(serviceTemplateEntity);
    }

    @Test
    void testUpdateServiceTemplateByUrl_OclLoaderThrowsException() throws Exception {
        when(mockOclLoader.getOcl(URI.create(oclLocation).toURL())).thenThrow(Exception.class);
        assertThatThrownBy(() -> serviceTemplateManageTest.updateServiceTemplateByUrl(
                UUID.randomUUID().toString(), oclLocation))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testUpdateServiceTemplate() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ocl.setVersion("2.1");
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        when(mockStorage.storeAndFlush(any())).thenReturn(serviceTemplateEntity);
        TerraformLocalDeployment deployment =
                new TerraformLocalDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, terraformProviderHelper, taskExecutor,
                        terraformDeploymentResultCallbackManager, deployServiceEntityHandler,
                        scriptsGitRepoManage);
        doReturn(deployment).when(deployerKindManager).getDeployment(any());
        doReturn("""
                terraform {
                      required_providers {
                        huaweicloud = {
                          source = "huaweicloud/huaweicloud"
                          version = "~> 1.51.0"
                        }
                      }
                    }
                                
                    provider "huaweicloud" {
                      region = "test"
                        }
                        """).when(this.terraformProviderHelper)
                .getProvider(any(Csp.class), any());
        when(identityProviderManager.getUserNamespace()).thenReturn(Optional.of("ISV-A"));
        ServiceTemplateEntity updateServiceTemplateEntity =
                serviceTemplateManageTest.updateServiceTemplate(uuid.toString(), ocl);
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updateServiceTemplateEntity.getServiceRegistrationState());
        verify(serviceTemplateOpenApiGenerator).updateServiceApi(serviceTemplateEntity);
    }

    @Test
    void testUpdateThrowsServiceTemplateUpdateNotAllowedException() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ocl.setServiceVersion("1.0");
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        when(identityProviderManager.getUserNamespace()).thenReturn(Optional.of("ISV-A"));
        Assertions.assertThrows(ServiceTemplateUpdateNotAllowed.class, () ->
                serviceTemplateManageTest.updateServiceTemplate(uuid.toString(), ocl));
    }


    @Test
    void testUpdateThrowsAccessDeniedException() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setNamespace("ISV-A");
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        when(identityProviderManager.getUserNamespace()).thenReturn(Optional.empty());
        Assertions.assertThrows(AccessDeniedException.class, () ->
                serviceTemplateManageTest.updateServiceTemplate(uuid.toString(), ocl));
    }


    @Test
    void testUpdateThrowsServiceTemplateNotRegisteredException() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ocl.setServiceVersion("1.0");
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(null);
        Assertions.assertThrows(ServiceTemplateNotRegistered.class, () ->
                serviceTemplateManageTest.updateServiceTemplate(uuid.toString(), ocl));
    }

    @Test
    void testRegisterServiceTemplate() {
        TerraformLocalDeployment deployment =
                new TerraformLocalDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, terraformProviderHelper, taskExecutor,
                        terraformDeploymentResultCallbackManager, deployServiceEntityHandler,
                        scriptsGitRepoManage);
        doReturn(deployment).when(deployerKindManager).getDeployment(any());
        doReturn("""
                    terraform {
                  required_providers {
                    huaweicloud = {
                      source = "huaweicloud/huaweicloud"
                      version = "~> 1.51.0"
                    }
                  }
                }
                            
                provider "huaweicloud" {
                      region = "test"
                    }
                    """).when(this.terraformProviderHelper)
                .getProvider(any(Csp.class), any());
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        when(mockStorage.storeAndFlush(any())).thenReturn(serviceTemplateEntity);

        ServiceTemplateEntity savedServiceTemplateEntity =
                serviceTemplateManageTest.registerServiceTemplate(oclRegister);
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                savedServiceTemplateEntity.getServiceRegistrationState());
        verify(serviceTemplateOpenApiGenerator).generateServiceApi(savedServiceTemplateEntity);
    }

    @Test
    void testRegisterThrowsServiceTemplateAlreadyRegistered() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ServiceTemplateEntity entity = new ServiceTemplateEntity();
        entity.setName(StringUtils.lowerCase(ocl.getName()));
        entity.setVersion(StringUtils.lowerCase(ocl.getServiceVersion()));
        entity.setCsp(ocl.getCloudServiceProvider().getName());
        entity.setCategory(ocl.getCategory());
        entity.setServiceHostingType(ServiceHostingType.SELF);
        entity.setOcl(ocl);
        entity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);

        ServiceTemplateEntity existedServiceTemplateEntity = getServiceTemplateEntity();

        when(mockStorage.findServiceTemplate(entity)).thenReturn(existedServiceTemplateEntity);

        Assertions.assertThrows(ServiceTemplateAlreadyRegistered.class, () ->
                serviceTemplateManageTest.registerServiceTemplate(ocl));
    }


    @Test
    void testRegisterIconThrowsProcessingFailedException() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ocl.setIcon(
                "https://raw.githubusercontent.com/eclipse-xpanse/xpanse/main/static/full-logo.png");
        Assertions.assertThrows(IconProcessingFailedException.class, () ->
                serviceTemplateManageTest.registerServiceTemplate(ocl));
    }

    @Test
    void testRegisterServiceTemplateByUrl() throws Exception {
        when(mockOclLoader.getOcl(URI.create(oclLocation).toURL())).thenReturn(oclRegister);
        TerraformLocalDeployment deployment =
                new TerraformLocalDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, terraformProviderHelper, taskExecutor,
                        terraformDeploymentResultCallbackManager, deployServiceEntityHandler,
                        scriptsGitRepoManage);
        doReturn(deployment).when(deployerKindManager).getDeployment(any());
        doReturn("""
                terraform {
                  required_providers {
                    huaweicloud = {
                          source = "huaweicloud/huaweicloud"
                          version = "~> 1.51.0"
                        }
                      }
                    }
                                
                    provider "huaweicloud" {
                      region = "test"
                    }
                    """).when(this.terraformProviderHelper)
                .getProvider(any(Csp.class), any());

        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        when(mockStorage.storeAndFlush(any())).thenReturn(serviceTemplateEntity);

        ServiceTemplateEntity savedServiceTemplateEntity =
                serviceTemplateManageTest.registerServiceTemplateByUrl(oclLocation);
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                savedServiceTemplateEntity.getServiceRegistrationState());
        verify(serviceTemplateOpenApiGenerator).generateServiceApi(savedServiceTemplateEntity);
    }

    @Test
    void testServiceTemplateByUrl_OclLoaderThrowsException() throws Exception {
        when(mockOclLoader.getOcl(URI.create(oclLocation).toURL())).thenThrow(Exception.class);
        assertThatThrownBy(() -> serviceTemplateManageTest.registerServiceTemplateByUrl(
                oclLocation)).isInstanceOf(Exception.class);
    }

    @Test
    void testGetServiceTemplate() {
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();

        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        ServiceTemplateEntity ServiceTemplate =
                serviceTemplateManageTest.getServiceTemplateDetails(uuid.toString(), false);
        Assertions.assertEquals(serviceTemplateEntity, ServiceTemplate);
    }


    @Test
    void testGetServiceTemplateThrowsServiceTemplateNotRegistered() {
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(null);
        Assertions.assertThrows(ServiceTemplateNotRegistered.class, () ->
                serviceTemplateManageTest.getServiceTemplateDetails(uuid.toString(), false));

    }

    @Test
    void testListServiceTemplates() {
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        List<ServiceTemplateEntity> ServiceTemplateEntities = new ArrayList<>();
        ServiceTemplateEntities.add(serviceTemplateEntity);
        when(mockStorage.listServiceTemplates(any())).thenReturn(
                ServiceTemplateEntities);

        List<ServiceTemplateEntity> ServiceTemplateEntities1 =
                serviceTemplateManageTest.listServiceTemplates(
                        Category.MIDDLEWARE, Csp.HUAWEI, "kafka", "v3.1.1", null, true);
        Assertions.assertEquals(ServiceTemplateEntities, ServiceTemplateEntities1);
    }

    @Test
    void testListServiceTemplates_ServiceTemplateStorageReturnsNoItems() {
        ServiceTemplateQueryModel query = new ServiceTemplateQueryModel();
        query.setCsp(Csp.HUAWEI);
        query.setCategory(Category.MIDDLEWARE);
        query.setServiceName("kafka");
        query.setServiceVersion("v3.1.1");

        ServiceTemplateQueryModel query1 = new ServiceTemplateQueryModel();
        query1.setCsp(Csp.HUAWEI);
        query1.setCategory(Category.MIDDLEWARE);
        query1.setServiceName("kafka");
        query1.setServiceVersion("v3.1.1");
        when(mockStorage.listServiceTemplates(query1)).thenReturn(Collections.emptyList());

        List<ServiceTemplateEntity> result =
                serviceTemplateManageTest.listServiceTemplates(
                        Category.MIDDLEWARE, Csp.HUAWEI, "kafka", "v3.1.1", null, true);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testUnregisterServiceTemplate() {
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        when(mockStorage.getServiceTemplateById(serviceTemplateEntity.getId())).thenReturn(
                serviceTemplateEntity);
        when(identityProviderManager.getUserNamespace()).thenReturn(Optional.of("ISV-A"));
        serviceTemplateManageTest.unregisterServiceTemplate(
                serviceTemplateEntity.getId().toString());
        verify(mockStorage).removeById(serviceTemplateEntity.getId());
        verify(serviceTemplateOpenApiGenerator).deleteServiceApi(
                serviceTemplateEntity.getId().toString());
    }

    @Test
    void testUnregisterThrowsServiceTemplateNotRegistered() {
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(null);
        Assertions.assertThrows(ServiceTemplateNotRegistered.class, () ->
                serviceTemplateManageTest.unregisterServiceTemplate(uuid.toString()));

    }


    @Test
    void testGetOpenApiUrl() {
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity();
        when(mockStorage.getServiceTemplateById(uuid))
                .thenReturn(serviceTemplateEntity);
        when(serviceTemplateOpenApiGenerator.getOpenApi(serviceTemplateEntity)).thenReturn(
                "result");
        String result =
                serviceTemplateManageTest.getOpenApiUrl(uuid.toString());
        Assertions.assertEquals(result, "result");
    }

    @Test
    void testGetOpenApiUrlThrowsServiceTemplateNotRegistered() {
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(null);
        Assertions.assertThrows(ServiceTemplateNotRegistered.class, () ->
                serviceTemplateManageTest.getOpenApiUrl(uuid.toString()));

    }
}
