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
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.TerraformDeployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.query.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
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
    private PluginManager pluginManager;
    @Mock
    private ServiceTemplateStorage mockStorage;
    @Mock
    private OclLoader mockOclLoader;
    @Mock
    private ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator;
    @Mock
    private DeployService mockDeployService;
    @Mock
    private IdentityProviderManager identityProviderManager;
    @Mock
    private OpenApiUrlManage openApiUrlManage;
    @Mock
    DeployServiceStorage deployServiceStorage;
    @Mock
    DeployResourceStorage deployResourceStorage;
    @Mock
    TerraformLocalConfig terraformLocalConfig;
    @Mock
    ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;
    @InjectMocks
    private ServiceTemplateManage serviceTemplateManageTest;

    @BeforeAll
    static void init() throws Exception {
        oclLoader = new OclLoader();
        oclRegister = oclLoader.getOcl(URI.create(oclLocation).toURL());
        uuid = UUID.fromString("ed6248d4-2bcd-4e94-84b0-29e014c05137");
    }

    @Test
    void testUpdateServiceTemplateByUrl() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ocl.setVersion("2.1");

        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setOcl(oclRegister);
        serviceTemplateEntity.setServiceHostingType(oclRegister.getServiceHostingType());

        when(mockOclLoader.getOcl(URI.create(oclLocation).toURL())).thenReturn(ocl);
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        TerraformDeployment deployment =
                new TerraformDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, pluginManager);

        doReturn(deployment).when(mockDeployService).getDeployment(any());
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
            """).when(this.pluginManager).getTerraformProviderForRegionByCsp(any(Csp.class), any());
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
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setServiceHostingType(oclRegister.getServiceHostingType());
        serviceTemplateEntity.setOcl(oclRegister);

        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        TerraformDeployment deployment =
                new TerraformDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, pluginManager);
        doReturn(deployment).when(mockDeployService).getDeployment(any());
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
                        """).when(this.pluginManager)
                .getTerraformProviderForRegionByCsp(any(Csp.class), any());
        ServiceTemplateEntity updateServiceTemplateEntity =
                serviceTemplateManageTest.updateServiceTemplate(uuid.toString(), ocl);
        log.error(updateServiceTemplateEntity.toString());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updateServiceTemplateEntity.getServiceRegistrationState());
        verify(serviceTemplateOpenApiGenerator).updateServiceApi(serviceTemplateEntity);
    }

    @Test
    void testUpdateThrowsServiceTemplateUpdateNotAllowedException() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ocl.setServiceVersion("1.0");
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setOcl(oclRegister);
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        Assertions.assertThrows(ServiceTemplateUpdateNotAllowed.class, () ->
                serviceTemplateManageTest.updateServiceTemplate(uuid.toString(), ocl));
    }


    @Test
    void testUpdateThrowsAccessDeniedException() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setNamespace("ISV-A");
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);
        CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId("ISV-B");
        currentUserInfo.setNamespace("ISV-B");
        when(identityProviderManager.getCurrentUserInfo()).thenReturn(currentUserInfo);
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
        TerraformDeployment deployment =
                new TerraformDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, pluginManager);
        doReturn(deployment).when(mockDeployService).getDeployment(any());
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
                    """).when(this.pluginManager)
                .getTerraformProviderForRegionByCsp(any(Csp.class), any());
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateManageTest.registerServiceTemplate(oclRegister);
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                serviceTemplateEntity.getServiceRegistrationState());
        verify(serviceTemplateOpenApiGenerator).generateServiceApi(serviceTemplateEntity);
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

        ServiceTemplateEntity existedServiceTemplateEntity = new ServiceTemplateEntity();

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
        TerraformDeployment deployment =
                new TerraformDeployment(new DeployEnvironments(null, null, null, null),
                        terraformLocalConfig, pluginManager);
        doReturn(deployment).when(mockDeployService).getDeployment(any());
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
            """).when(this.pluginManager).getTerraformProviderForRegionByCsp(any(Csp.class), any());
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateManageTest.registerServiceTemplateByUrl(oclLocation);
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                serviceTemplateEntity.getServiceRegistrationState());
        verify(serviceTemplateOpenApiGenerator).generateServiceApi(serviceTemplateEntity);
    }

    @Test
    void testServiceTemplateByUrl_OclLoaderThrowsException() throws Exception {
        when(mockOclLoader.getOcl(URI.create(oclLocation).toURL())).thenThrow(Exception.class);
        assertThatThrownBy(() -> serviceTemplateManageTest.registerServiceTemplateByUrl(
                oclLocation)).isInstanceOf(Exception.class);
    }

    @Test
    void testGetServiceTemplate() {
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setOcl(oclRegister);

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
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setOcl(oclRegister);
        List<ServiceTemplateEntity> ServiceTemplateEntities = new ArrayList<>();
        ServiceTemplateEntities.add(serviceTemplateEntity);
        when(mockStorage.listServiceTemplates(new ServiceTemplateQueryModel())).thenReturn(
                ServiceTemplateEntities);

        List<ServiceTemplateEntity> ServiceTemplateEntities1 =
                serviceTemplateManageTest.listServiceTemplates(new ServiceTemplateQueryModel());
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
                serviceTemplateManageTest.listServiceTemplates(query);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testUnregisterServiceTemplate() {
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setOcl(oclRegister);
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(serviceTemplateEntity);

        serviceTemplateManageTest.unregisterServiceTemplate(uuid.toString());
        verify(mockStorage).removeById(uuid);
        verify(serviceTemplateOpenApiGenerator).deleteServiceApi(uuid.toString());
    }

    @Test
    void testUnregisterThrowsServiceTemplateNotRegistered() {
        when(mockStorage.getServiceTemplateById(uuid)).thenReturn(null);
        Assertions.assertThrows(ServiceTemplateNotRegistered.class, () ->
                serviceTemplateManageTest.unregisterServiceTemplate(uuid.toString()));

    }


    @Test
    void testGetOpenApiUrl() {
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setName(oclRegister.getName());
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setCategory(oclRegister.getCategory());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        serviceTemplateEntity.setVersion(oclRegister.getServiceVersion());
        serviceTemplateEntity.setCsp(oclRegister.getCloudServiceProvider().getName());
        serviceTemplateEntity.setOcl(oclRegister);

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
