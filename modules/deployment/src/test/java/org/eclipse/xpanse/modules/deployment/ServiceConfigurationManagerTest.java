/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationManage;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for ServiceConfigurationManager.
 */
@ExtendWith(MockitoExtension.class)
public class ServiceConfigurationManagerTest {

    @InjectMocks
    private ServiceConfigurationManager serviceConfigurationManager;

    @Mock
    private DeployServiceEntityHandler deployServiceEntityHandler;

    @Mock
    private ServiceConfigurationUpdateStorage serviceConfigurationUpdateStorage;

    @Mock
    private ServiceTemplateStorage serviceTemplateStorage;

    @Mock
    private DeployService deployService;

    @Mock
    private ServiceOrderStorage serviceOrderStorage;

    @Mock
    private UserServiceHelper userServiceHelper;

    @Mock
    private ServiceConfigurationVariablesJsonSchemaValidator serviceConfigurationVariablesJsonSchemaValidator;

    @Mock
    private ServiceConfigurationVariablesJsonSchemaGenerator serviceConfigurationVariablesJsonSchemaGenerator;

    @Mock
    private CustomRequestIdGenerator customRequestIdGenerator;

    private  static Map<String, Object> CONFIGURATION = Collections.singletonMap("param1", "value1");


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testChangeServiceConfiguration_Success() {
        String serviceId = UUID.randomUUID().toString();
        ServiceConfigurationUpdate configurationUpdate = new ServiceConfigurationUpdate();
        configurationUpdate.setConfiguration(CONFIGURATION);

        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());

        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        Ocl ocl = new Ocl();
        ocl.setServiceConfigurationManage(new ServiceConfigurationManage());
        serviceTemplateEntity.setOcl(ocl);
        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(deployServiceEntity);
        when(serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(serviceTemplateEntity);
        ServiceOrder
                serviceOrder = serviceConfigurationManager.changeServiceConfiguration(serviceId, configurationUpdate);

        assertNotNull(serviceOrder);
    }

    @Test
    void testChangeServiceConfiguration_ServiceTemplateNotFound() {

        String serviceId = UUID.randomUUID().toString();
        ServiceConfigurationUpdate configurationUpdate = new ServiceConfigurationUpdate();
        configurationUpdate.setConfiguration(CONFIGURATION);

        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(new DeployServiceEntity());
        when(serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(null);
        ServiceTemplateNotRegistered exception = assertThrows(ServiceTemplateNotRegistered.class, () ->
                serviceConfigurationManager.changeServiceConfiguration(serviceId, configurationUpdate));
        assertTrue(exception.getMessage().contains("Service template with id"));
    }

    @Test
    void testChangeServiceConfiguration_ValidationFailure() {
        // Arrange
        String serviceId = UUID.randomUUID().toString();
        ServiceConfigurationUpdate configurationUpdate = new ServiceConfigurationUpdate();
        configurationUpdate.setConfiguration(CONFIGURATION);

        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());

        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        Ocl ocl = new Ocl();
        ocl.setServiceConfigurationManage(new ServiceConfigurationManage());
        serviceTemplateEntity.setOcl(ocl);

        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(deployServiceEntity);
        when(serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(serviceTemplateEntity);

        doThrow(new ServiceConfigurationInvalidException("Invalid configuration"))
                .when(serviceConfigurationVariablesJsonSchemaValidator)
                .validateServiceConfiguration(any(), any(), any());

        ServiceConfigurationInvalidException exception = assertThrows(ServiceConfigurationInvalidException.class,
                () -> serviceConfigurationManager.changeServiceConfiguration(serviceId, configurationUpdate));
        assertTrue(exception.getMessage().contains("Change service configuration error"));
    }

    @Test
    void testChangeServiceConfiguration_EmptyUpdate() {
        // Arrange
        String serviceId = UUID.randomUUID().toString();
        ServiceConfigurationUpdate configurationUpdate = new ServiceConfigurationUpdate();
        configurationUpdate.setConfiguration(Collections.emptyMap());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                serviceConfigurationManager.changeServiceConfiguration(serviceId, configurationUpdate));
        assertEquals("Parameter ServiceConfigurationUpdate is empty", exception.getMessage());
    }
}