/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.deployment.deployers.terraform.TerraformDeployment.STATE_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.query.ServiceQueryModel;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

/**
 * Test of DeployService.
 */
@ExtendWith(MockitoExtension.class)
class DeployServiceTest {

    private static final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private static final String userId = "defaultUserId";
    private static DeployTask deployTask;
    private static Deployment deploymentMock;
    private static ServiceTemplateEntity serviceEntity;
    private static DeployServiceEntity deployServiceEntity;
    private static DeployResult deployResult;
    private static List<DeployResourceEntity> deployResourceEntities;
    private static DeployRequest deployRequest;
    private static org.eclipse.xpanse.modules.models.servicetemplate.Deployment deployment;

    @Mock
    private AesUtil aesUtil;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ServiceTemplateStorage serviceTemplateStorage;

    @Mock
    private DeployServiceStorage deployServiceStorage;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private IdentityProviderManager identityProviderManager;

    @Mock
    private ServiceVariablesJsonSchemaValidator serviceVariablesJsonSchemaValidator;

    @InjectMocks
    private DeployService deployService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        deployment =
                new org.eclipse.xpanse.modules.models.servicetemplate.Deployment();
        deployment.setKind(DeployerKind.TERRAFORM);
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("test");
        deployVariable.setMandatory(false);
        deployVariable.setValue("test");
        deployVariable.setSensitiveScope(SensitiveScope.ALWAYS);
        deployment.setVariables(List.of(deployVariable));

        Map<String, Object> requestProperties = new HashMap<>();
        requestProperties.put("test", "test");
        Ocl ocl = new Ocl();
        ocl.setName("oclName");
        ocl.setDeployment(deployment);

        deployRequest = new DeployRequest();
        deployRequest.setId(uuid);
        deployRequest.setUserId(userId);
        deployRequest.setCategory(Category.COMPUTE);
        deployRequest.setCsp(Csp.HUAWEI);
        deployRequest.setServiceName("service");
        deployRequest.setCustomerServiceName("customerService");
        deployRequest.setVersion("1.0");
        deployRequest.setOcl(ocl);
        deployRequest.setFlavor("flavor");
        deployRequest.setServiceRequestProperties(requestProperties);
        deployTask = new DeployTask();
        deployTask.setId(uuid);
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(deployRequest);

        deploymentMock = mock(Deployment.class);

        serviceEntity = new ServiceTemplateEntity();
        serviceEntity.setName("service");
        serviceEntity.setVersion("1.0");
        serviceEntity.setCsp(Csp.HUAWEI);
        serviceEntity.setCategory(Category.COMPUTE);
        serviceEntity.setOcl(ocl);

        deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setId(uuid);
        deployServiceEntity.setName("deployServiceEntity");
        deployServiceEntity.setCsp(Csp.HUAWEI);
        deployServiceEntity.setUserId(userId);
        deployServiceEntity.setDeployRequest(deployRequest);
        deployServiceEntity.setProperties(Map.of("key", "value"));
        deployServiceEntity.setPrivateProperties(Map.of("key", "value"));
        deployResourceEntities = new ArrayList<>();
        DeployResourceEntity resourceEntity = new DeployResourceEntity();
        resourceEntity.setId(UUID.randomUUID());
        resourceEntity.setName("resourceEntity");
        resourceEntity.setKind(DeployResourceKind.VM);
        resourceEntity.setProperties(Map.of("key", "value"));
        deployResourceEntities.add(resourceEntity);
        deployServiceEntity.setDeployResourceList(deployResourceEntities);

        deployResult = new DeployResult();
        deployResult.setId(uuid);
        DeployResource deployResource = new DeployResource();
        deployResource.setResourceId(uuid.toString());
        deployResource.setName("deployResource");
        deployResult.setResources(List.of(deployResource));
    }

    @Test
    void testDeploymentMap() {
        when(deploymentMock.getDeployerKind()).thenReturn(DeployerKind.TERRAFORM);

        Map<String, Deployment> deploymentBeans = new HashMap<>();
        deploymentBeans.put("deploymentBean", deploymentMock);

        when(applicationContext.getBeansOfType(Deployment.class)).thenReturn(deploymentBeans);

        Map<DeployerKind, Deployment> deploymentMap = deployService.deploymentMap();

        assertFalse(deploymentMap.isEmpty());
        assertTrue(deploymentMap.containsKey(DeployerKind.TERRAFORM));
        assertEquals(deploymentMock, deploymentMap.get(DeployerKind.TERRAFORM));
    }

    @Test
    void testGetNewDeployServiceTask() {
        DeployServiceEntity entity = deployService.getNewDeployServiceTask(deployTask);

        assertNotNull(entity);
        assertEquals(deployTask.getId(), entity.getId());
        assertNotNull(entity.getCreateTime());
        assertEquals("1.0", entity.getVersion());
        assertEquals("service", entity.getName());
        assertEquals(Csp.HUAWEI, entity.getCsp());
        assertEquals(Category.COMPUTE, entity.getCategory());
        assertEquals("customerService", entity.getCustomerServiceName());
        assertEquals("flavor", entity.getFlavor());
        assertEquals("defaultUserId", entity.getUserId());
        assertEquals(deployTask.getDeployRequest(), entity.getDeployRequest());
        assertTrue(CollectionUtils.isEmpty(entity.getDeployResourceList()));
    }

    @Test
    void testGetDeployHandler() {
        Map<Csp, OrchestratorPlugin> pluginsMap = new HashMap<>();
        OrchestratorPlugin plugin = mock(OrchestratorPlugin.class);
        DeployResourceHandler resourceHandler = mock(DeployResourceHandler.class);
        when(plugin.getResourceHandler()).thenReturn(resourceHandler);
        pluginsMap.put(Csp.HUAWEI, plugin);
        when(pluginManager.getPluginsMap()).thenReturn(pluginsMap);

        when(serviceTemplateStorage.findServiceTemplate(any(ServiceTemplateEntity.class)))
                .thenReturn(serviceEntity);

        when(deploymentMock.getDeployerKind()).thenReturn(DeployerKind.TERRAFORM);

        Map<String, Deployment> deploymentBeans = new HashMap<>();
        deploymentBeans.put("deploymentBean", deploymentMock);

        when(applicationContext.getBeansOfType(Deployment.class)).thenReturn(deploymentBeans);
        doNothing().when(serviceVariablesJsonSchemaValidator)
                .validateDeployVariables(anyList(), anyMap(), any());

        deployService.deploymentMap();

        Deployment result = deployService.getDeployHandler(deployTask);

        assertNotNull(result);
        assertEquals(DeployerKind.TERRAFORM, result.getDeployerKind());
        assertEquals(serviceEntity.getOcl(), deployTask.getOcl());
        assertEquals(serviceEntity.getOcl(), deployTask.getDeployRequest().getOcl());
    }

    @Test
    void testGetDeployHandler_PluginNotFoundException() {
        serviceEntity.getOcl().setDeployment(null);

        Map<Csp, OrchestratorPlugin> pluginsMap = new HashMap<>();
        OrchestratorPlugin plugin = mock(OrchestratorPlugin.class);
        pluginsMap.put(Csp.HUAWEI, plugin);
        when(pluginManager.getPluginsMap()).thenReturn(pluginsMap);

        when(serviceTemplateStorage.findServiceTemplate(any(ServiceTemplateEntity.class)))
                .thenReturn(serviceEntity);

        Deployment deploymentMock = mock(Deployment.class);
        when(deploymentMock.getDeployerKind()).thenReturn(DeployerKind.TERRAFORM);

        Map<String, Deployment> deploymentBeans = new HashMap<>();
        deploymentBeans.put("deploymentBean", deploymentMock);

        when(applicationContext.getBeansOfType(Deployment.class)).thenReturn(deploymentBeans);

        deployService.deploymentMap();

        assertThrows(PluginNotFoundException.class,
                () -> deployService.getDeployHandler(deployTask));
    }

    @Test
    void testGetDeployHandler_ServiceNotRegisteredException() {
        when(serviceTemplateStorage.findServiceTemplate(any(ServiceTemplateEntity.class)))
                .thenReturn(null);

        assertThrows(ServiceTemplateNotRegistered.class,
                () -> deployService.getDeployHandler(deployTask));
    }

    @Test
    void testAsyncDestroyService() {
        deployResult.setState(TerraformExecState.DESTROY_SUCCESS);

        String stateFile = deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.asyncDestroyService(deploymentMock, deployTask);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).findDeployServiceById(uuid);
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME));
        verify(deployServiceStorage, times(1)).storeAndFlush(deployServiceEntity);
    }

    @Test
    void testAsyncDestroyService_Exception() {
        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);

        deployService.asyncDestroyService(deploymentMock, deployTask);

        doThrow(new RuntimeException()).when(deployServiceStorage)
                .storeAndFlush(deployServiceEntity);

        assertThrows(RuntimeException.class,
                () -> deployService.asyncDestroyService(deploymentMock, deployTask));
    }

    @Test
    void testAsyncDestroyService_ServiceNotDeployedException() {
        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(null);

        assertThrows(ServiceNotDeployedException.class,
                () -> deployService.asyncDestroyService(deploymentMock, deployTask));
    }

    @Test
    void testAsyncDestroyService_resourcesEmpty() {
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
        deployResult.setResources(null);
        deployResult.setState(TerraformExecState.DESTROY_SUCCESS);

        String stateFile = deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);

        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.asyncDestroyService(deploymentMock, deployTask);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).findDeployServiceById(deployTask.getId());
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME));
        verify(deployServiceStorage, times(1)).storeAndFlush(deployServiceEntity);
    }


    @Test
    void testAsyncDestroyService_DESTROY_FAILED() {
        deployResult.setState(TerraformExecState.DESTROY_FAILED);
        String stateFile = deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);

        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.asyncDestroyService(deploymentMock, deployTask);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).findDeployServiceById(deployTask.getId());
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME));
        verify(deployServiceStorage, times(1)).storeAndFlush(deployServiceEntity);
    }

    @Test
    void testListDeployedServices() {
        List<DeployServiceEntity> deployServices = new ArrayList<>();
        deployServices.add(deployServiceEntity);

        when(deployServiceStorage.listServices(any())).thenReturn(deployServices);
        List<ServiceVo> result = deployService.listDeployedServices(new ServiceQueryModel());

        assertEquals(1, result.size());

        ServiceVo serviceVo = result.get(0);
        assertEquals(uuid, serviceVo.getId());
        assertEquals("deployServiceEntity", serviceVo.getName());
    }

    @Test
    void testGetDeployServiceDetails_ValidIdAndUser_ReturnsServiceDetailVo() {
        when(deployServiceStorage.findDeployServiceById(uuid))
                .thenReturn(deployServiceEntity);
        when(identityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));
        ServiceDetailVo result = deployService.getDeployServiceDetails(uuid);

        assertNotNull(result);
        assertEquals(uuid, result.getId());
        assertEquals("deployServiceEntity", result.getName());
        assertEquals(deployResourceEntities.size(), result.getDeployResources().size());
        assertEquals(deployServiceEntity.getProperties().size(),
                result.getDeployedServiceProperties().size());
    }

    @Test
    void testGetDeployServiceDetails_ServiceNotDeployedException() {
        when(deployServiceStorage.findDeployServiceById(deployTask.getId()))
                .thenReturn(null);

        assertThrows(ServiceNotDeployedException.class,
                () -> deployService.getDeployServiceDetails(uuid));
    }

    @Test
    void testGetDestroyHandler_DeployedServiceNotFound_ThrowsServiceNotDeployedException() {
        when(deployServiceStorage.findDeployServiceById(deployTask.getId())).thenReturn(null);

        assertThrows(ServiceNotDeployedException.class,
                () -> deployService.getDestroyHandler(deployTask));
    }

    @Test
    void testGetDestroyHandler_ServiceStateIsDestroying_ThrowsInvalidServiceStateException() {
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);

        when(identityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        when(deployServiceStorage.findDeployServiceById(deployTask.getId())).thenReturn(
                deployServiceEntity);
        assertThrows(InvalidServiceStateException.class,
                () -> deployService.getDestroyHandler(deployTask));
    }

    @Test
    void testGetDestroyHandler() {
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);

        when(deployServiceStorage.findDeployServiceById(deployTask.getId())).thenReturn(
                deployServiceEntity);

        // Mocking the PluginManager
        Map<Csp, OrchestratorPlugin> pluginsMap = new HashMap<>();
        OrchestratorPlugin plugin = mock(OrchestratorPlugin.class);
        DeployResourceHandler resourceHandler = mock(DeployResourceHandler.class);
        when(plugin.getResourceHandler()).thenReturn(resourceHandler);
        pluginsMap.put(Csp.HUAWEI, plugin);
        when(pluginManager.getPluginsMap()).thenReturn(pluginsMap);

        Deployment deploymentMock = mock(Deployment.class);
        when(deploymentMock.getDeployerKind()).thenReturn(DeployerKind.TERRAFORM);

        Map<String, Deployment> deploymentBeans = new HashMap<>();
        deploymentBeans.put("deploymentBean", deploymentMock);

        when(applicationContext.getBeansOfType(Deployment.class)).thenReturn(deploymentBeans);
        deployService.deploymentMap();

        when(identityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        Deployment result = deployService.getDestroyHandler(deployTask);

        // Verify the interactions and assertions
        verify(deployServiceStorage).findDeployServiceById(uuid);
        assertEquals(deployment, deployTask.getOcl().getDeployment());
        assertNotNull(result);
        assertEquals(deployment.getKind(), result.getDeployerKind());
    }

    @Test
    void testAsyncDeployService() {
        when(deploymentMock.deploy(deployTask)).thenReturn(deployResult);

        deployService.asyncDeployService(deploymentMock, deployTask);

        // Verify the interactions and assertions
        verify(deployServiceStorage, times(1)).storeAndFlush(any(DeployServiceEntity.class));

        // Verify the captured DeployServiceEntity
        ArgumentCaptor<DeployServiceEntity> entityCaptor =
                ArgumentCaptor.forClass(DeployServiceEntity.class);
        verify(deployServiceStorage, times(1)).storeAndFlush(entityCaptor.capture());
        DeployServiceEntity capturedEntity = entityCaptor.getValue();
        assertEquals(ServiceDeploymentState.DEPLOYING,
                capturedEntity.getServiceDeploymentState());
        assertNotEquals(deployResult.getProperties(), capturedEntity.getProperties());
        assertNotEquals(deployResult.getPrivateProperties(), capturedEntity.getPrivateProperties());
        assertEquals("test", capturedEntity.getDeployRequest().getServiceRequestProperties().get("test"));
    }

    @Test
    void testAsyncDeployService_RuntimeException() {
        when(deploymentMock.deploy(deployTask)).thenThrow(new RuntimeException());

        deployService.asyncDeployService(deploymentMock, deployTask);

        // Verify the interactions and assertions
        verify(deployServiceStorage, times(2)).storeAndFlush(any(DeployServiceEntity.class));

        // Verify the captured DeployServiceEntity
        ArgumentCaptor<DeployServiceEntity> entityCaptor =
                ArgumentCaptor.forClass(DeployServiceEntity.class);
        verify(deployServiceStorage, times(2)).storeAndFlush(entityCaptor.capture());
        DeployServiceEntity capturedEntity = entityCaptor.getValue();
        assertEquals(ServiceDeploymentState.DEPLOY_FAILED,
                capturedEntity.getServiceDeploymentState());
        assertTrue(capturedEntity.getProperties().isEmpty());
        assertTrue(capturedEntity.getPrivateProperties().isEmpty());
    }

    @Test
    void testGetDeployment() {
        DeployService deployService = new DeployService();
        Assertions.assertThrows(DeployerNotFoundException.class, () ->
                deployService.getDeployment(DeployerKind.TERRAFORM));
    }

    @Test
    void testPurgeServiceSuccess() {
        deployServiceEntity.setServiceDeploymentState(
                ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED);
        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        String stateFile = deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.purgeService(deploymentMock, deployTask);
        // Verify the expected interactions
        verify(deployServiceStorage, times(2)).findDeployServiceById(uuid);
        verify(deployServiceStorage, times(1)).deleteDeployService(deployServiceEntity);
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME));
        verify(deployServiceStorage, times(1)).storeAndFlush(deployServiceEntity);
    }

    @Test
    void testPurgeServiceRefuse() {
        when(deployServiceStorage.findDeployServiceById(deployTask.getId())).thenReturn(
                deployServiceEntity);

        Assertions.assertThrows(InvalidServiceStateException.class, () ->
                deployService.purgeService(deploymentMock, deployTask));
    }

    @Test
    void testAsyncPurgeServiceSuccess() {
        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        String stateFile = deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.asyncPurgeService(deploymentMock, deployTask, deployServiceEntity);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).findDeployServiceById(uuid);
        verify(deployServiceStorage, times(1)).deleteDeployService(deployServiceEntity);
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME));
        verify(deployServiceStorage, times(1)).storeAndFlush(deployServiceEntity);
    }

    @Test
    void testAsyncPurgeService_NoReources() {
        deployServiceEntity.setDeployResourceList(Arrays.asList());

        deployService.asyncPurgeService(deploymentMock, deployTask, deployServiceEntity);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).deleteDeployService(deployServiceEntity);
    }

}
