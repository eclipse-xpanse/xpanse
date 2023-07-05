/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.register.RegisterServiceStorage;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceNotRegisteredException;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
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
    private static final String userName = "user";
    private static DeployTask deployTask;
    private static Deployment deploymentMock;
    private static RegisterServiceEntity serviceEntity;
    private static DeployServiceEntity deployServiceEntity;
    private static DeployResult deployResult;
    private static List<DeployResourceEntity> deployResourceEntities;
    private static CreateRequest createRequest;
    private static org.eclipse.xpanse.modules.models.service.register.Deployment deployment;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private RegisterServiceStorage registerServiceStorage;

    @Mock
    private DeployServiceStorage deployServiceStorage;

    @Mock
    private PluginManager pluginManager;

    @InjectMocks
    private DeployService deployService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        deployment =
                new org.eclipse.xpanse.modules.models.service.register.Deployment();
        deployment.setKind(DeployerKind.TERRAFORM);

        Ocl ocl = new Ocl();
        ocl.setName("oclName");
        ocl.setDeployment(deployment);

        createRequest = new CreateRequest();
        createRequest.setId(uuid);
        createRequest.setUserName(userName);
        createRequest.setCategory(Category.COMPUTE);
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setServiceName("service");
        createRequest.setCustomerServiceName("customerService");
        createRequest.setVersion("1.0");
        createRequest.setOcl(ocl);
        createRequest.setFlavor("flavor");

        deployTask = new DeployTask();
        deployTask.setId(uuid);
        deployTask.setOcl(ocl);
        deployTask.setCreateRequest(createRequest);

        deploymentMock = mock(Deployment.class);

        serviceEntity = new RegisterServiceEntity();
        serviceEntity.setName("service");
        serviceEntity.setVersion("1.0");
        serviceEntity.setCsp(Csp.HUAWEI);
        serviceEntity.setCategory(Category.COMPUTE);
        serviceEntity.setOcl(ocl);

        deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setId(uuid);
        deployServiceEntity.setName("deployServiceEntity");
        deployServiceEntity.setCsp(Csp.HUAWEI);
        deployServiceEntity.setUserName(userName);
        deployServiceEntity.setCreateRequest(createRequest);
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
        assertEquals("user", entity.getUserName());
        assertEquals(deployTask.getCreateRequest(), entity.getCreateRequest());
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

        when(registerServiceStorage.findRegisteredService(any(RegisterServiceEntity.class)))
                .thenReturn(serviceEntity);

        when(deploymentMock.getDeployerKind()).thenReturn(DeployerKind.TERRAFORM);

        Map<String, Deployment> deploymentBeans = new HashMap<>();
        deploymentBeans.put("deploymentBean", deploymentMock);

        when(applicationContext.getBeansOfType(Deployment.class)).thenReturn(deploymentBeans);

        Map<DeployerKind, Deployment> deploymentMap = deployService.deploymentMap();

        Deployment result = deployService.getDeployHandler(deployTask);

        assertNotNull(result);
        assertEquals(DeployerKind.TERRAFORM, result.getDeployerKind());
        assertEquals(serviceEntity.getOcl(), deployTask.getOcl());
        assertEquals(serviceEntity.getOcl(), deployTask.getCreateRequest().getOcl());
    }

    @Test
    void testGetDeployHandler_PluginNotFoundException() {
        serviceEntity.getOcl().setDeployment(null);

        Map<Csp, OrchestratorPlugin> pluginsMap = new HashMap<>();
        OrchestratorPlugin plugin = mock(OrchestratorPlugin.class);
        pluginsMap.put(Csp.HUAWEI, plugin);
        when(pluginManager.getPluginsMap()).thenReturn(pluginsMap);

        when(registerServiceStorage.findRegisteredService(any(RegisterServiceEntity.class)))
                .thenReturn(serviceEntity);

        Deployment deploymentMock = mock(Deployment.class);
        when(deploymentMock.getDeployerKind()).thenReturn(DeployerKind.TERRAFORM);

        Map<String, Deployment> deploymentBeans = new HashMap<>();
        deploymentBeans.put("deploymentBean", deploymentMock);

        when(applicationContext.getBeansOfType(Deployment.class)).thenReturn(deploymentBeans);

        Map<DeployerKind, Deployment> deploymentMap = deployService.deploymentMap();

        assertThrows(PluginNotFoundException.class,
                () -> deployService.getDeployHandler(deployTask));
    }

    @Test
    void testGetDeployHandler_ServiceNotRegisteredException() {
        when(registerServiceStorage.findRegisteredService(any(RegisterServiceEntity.class)))
                .thenReturn(null);

        assertThrows(ServiceNotRegisteredException.class,
                () -> deployService.getDeployHandler(deployTask));
    }

    @Test
    void testAsyncDestroyService() throws IOException {
        deployResult.setState(TerraformExecState.DESTROY_SUCCESS);

        String stateFile = deployServiceEntity.getPrivateProperties().get("stateFile");
        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.asyncDestroyService(deploymentMock, deployTask);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).findDeployServiceById(uuid);
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get("stateFile"));
        verify(deployServiceStorage, times(2)).storeAndFlush(deployServiceEntity);
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
    void testAsyncDestroyService_resourcesEmpty() throws IOException {
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
        deployResult.setResources(null);
        deployResult.setState(TerraformExecState.DESTROY_SUCCESS);

        String stateFile = deployServiceEntity.getPrivateProperties().get("stateFile");

        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.asyncDestroyService(deploymentMock, deployTask);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).findDeployServiceById(deployTask.getId());
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get("stateFile"));
        verify(deployServiceStorage, times(2)).storeAndFlush(deployServiceEntity);
    }


    @Test
    void testAsyncDestroyService_DESTROY_FAILED() throws IOException {
        deployResult.setState(TerraformExecState.DESTROY_FAILED);
        String stateFile = deployServiceEntity.getPrivateProperties().get("stateFile");

        when(deployServiceStorage.findDeployServiceById(uuid)).thenReturn(deployServiceEntity);
        when(deploymentMock.destroy(deployTask, stateFile)).thenReturn(deployResult);

        deployService.asyncDestroyService(deploymentMock, deployTask);

        // Verify the expected interactions
        verify(deployServiceStorage, times(1)).findDeployServiceById(deployTask.getId());
        verify(deploymentMock, times(1)).destroy(deployTask,
                deployServiceEntity.getPrivateProperties().get("stateFile"));
        verify(deployServiceStorage, times(2)).storeAndFlush(deployServiceEntity);
    }

    @Test
    public void testGetDeployedServices() {
        List<DeployServiceEntity> deployServices = new ArrayList<>();
        deployServices.add(deployServiceEntity);

        when(deployServiceStorage.services()).thenReturn(deployServices);

        List<ServiceVo> result = deployService.getDeployedServices();

        assertEquals(1, result.size());

        ServiceVo serviceVo = result.get(0);
        assertEquals(uuid, serviceVo.getId());
        assertEquals("deployServiceEntity", serviceVo.getName());
    }

    @Test
    public void testGetDeployServiceDetails_ValidIdAndUser_ReturnsServiceDetailVo() {
        when(deployServiceStorage.findDeployServiceById(uuid))
                .thenReturn(deployServiceEntity);

        ServiceDetailVo result = deployService.getDeployServiceDetails(uuid, userName);

        assertNotNull(result);
        assertEquals(uuid, result.getId());
        assertEquals("deployServiceEntity", result.getName());
        assertEquals(deployResourceEntities.size(), result.getDeployResources().size());
        assertEquals(deployServiceEntity.getProperties().size(),
                result.getDeployedServiceProperties().size());
    }

    @Test
    public void testGetDeployServiceDetails_ServiceNotDeployedException() {
        when(deployServiceStorage.findDeployServiceById(deployTask.getId()))
                .thenReturn(null);

        assertThrows(ServiceNotDeployedException.class,
                () -> deployService.getDeployServiceDetails(uuid, userName));
    }

    @Test
    public void testGetDestroyHandler_DeployedServiceNotFound_ThrowsServiceNotDeployedException() {
        when(deployServiceStorage.findDeployServiceById(deployTask.getId())).thenReturn(null);

        assertThrows(ServiceNotDeployedException.class,
                () -> deployService.getDestroyHandler(deployTask));
    }

    @Test
    public void testGetDestroyHandler_ServiceStateIsDestroying_ThrowsInvalidServiceStateException() {
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setCreateRequest(new CreateRequest());
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);

        when(deployServiceStorage.findDeployServiceById(deployTask.getId())).thenReturn(
                deployServiceEntity);

        assertThrows(InvalidServiceStateException.class,
                () -> deployService.getDestroyHandler(deployTask));
    }

    @Test
    public void testGetDestroyHandler() {
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

        Map<DeployerKind, Deployment> deploymentMap = deployService.deploymentMap();

        Deployment result = deployService.getDestroyHandler(deployTask);

        // Verify the interactions and assertions
        verify(deployServiceStorage).findDeployServiceById(uuid);
        assertEquals(deployment, deployTask.getOcl().getDeployment());
        assertNotNull(result);
        assertEquals(deployment.getKind(), result.getDeployerKind());
    }

    @Test
    public void testAsyncDeployService() {
        when(deploymentMock.deploy(deployTask)).thenReturn(deployResult);

        deployService.asyncDeployService(deploymentMock, deployTask);

        // Verify the interactions and assertions
        verify(deployServiceStorage, times(2)).storeAndFlush(any(DeployServiceEntity.class));

        // Verify the captured DeployServiceEntity
        ArgumentCaptor<DeployServiceEntity> entityCaptor =
                ArgumentCaptor.forClass(DeployServiceEntity.class);
        verify(deployServiceStorage, times(2)).storeAndFlush(entityCaptor.capture());
        DeployServiceEntity capturedEntity = entityCaptor.getValue();
        assertEquals(ServiceDeploymentState.DEPLOY_SUCCESS,
                capturedEntity.getServiceDeploymentState());
        assertEquals(deployResult.getProperties(), capturedEntity.getProperties());
        assertEquals(deployResult.getPrivateProperties(), capturedEntity.getPrivateProperties());
    }

    @Test
    public void testAsyncDeployService_RuntimeException() {
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
        assertNull(capturedEntity.getProperties());
        assertNull(capturedEntity.getPrivateProperties());
    }

    @Test
    void testGetDeployment() {
        DeployService deployService = new DeployService();

        Assertions.assertThrows(DeployerNotFoundException.class, () -> {
            deployService.getDeployment(DeployerKind.TERRAFORM);
        });
    }

}
