package org.eclipse.xpanse.modules.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceDeploymentEntityConverterTest {
    @Mock private ServiceTemplateStorage serviceTemplateStorage;
    @InjectMocks private DeployServiceEntityConverter converter;
    private ServiceDeploymentEntity serviceDeploymentEntity;
    private ServiceTemplateEntity serviceTemplateEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceDeploymentEntity = new ServiceDeploymentEntity();
        serviceDeploymentEntity.setId(UUID.randomUUID());
        serviceDeploymentEntity.setDeployRequest(new DeployRequest());
        serviceDeploymentEntity.setServiceTemplateId(UUID.randomUUID());

        Ocl ocl = new Ocl();
        ocl.setCategory(Category.AI);
        ocl.setVersion("1.0");
        ocl.setName("Test Service");
        ocl.setServiceVersion("1.0");
        ocl.setDescription("Test Description");
        ocl.setNamespace("testNamespace");
        ocl.setIcon("icon.png");
        ocl.setCloudServiceProvider(new CloudServiceProvider());
        ocl.setDeployment(new Deployment());
        ocl.setFlavors(new FlavorsWithPrice());
        ocl.setBilling(new Billing());
        ocl.setServiceHostingType(ServiceHostingType.SELF);
        ocl.setServiceProviderContactDetails(new ServiceProviderContactDetails());
        ocl.setServiceConfigurationManage(new ServiceChangeManage());

        serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceDeploymentEntity.getServiceTemplateId());
        serviceTemplateEntity.setOcl(ocl);

        when(serviceTemplateStorage.getServiceTemplateById(
                        serviceDeploymentEntity.getServiceTemplateId()))
                .thenReturn(serviceTemplateEntity);
    }

    @Test
    void testGetDeployTaskByStoredService() {
        DeployTask deployTask =
                converter.getDeployTaskByStoredService(
                        ServiceOrderType.DEPLOY, serviceDeploymentEntity);

        assertNotNull(deployTask);
        assertEquals(ServiceOrderType.DEPLOY, deployTask.getTaskType());
        assertEquals(serviceDeploymentEntity.getId(), deployTask.getServiceId());
        assertEquals(serviceDeploymentEntity.getDeployRequest(), deployTask.getDeployRequest());
        assertEquals(serviceTemplateEntity.getOcl(), deployTask.getOcl());
        assertEquals(serviceTemplateEntity.getId(), deployTask.getServiceTemplateId());
    }

    @Test
    void testGetInitialServiceConfiguration() {
        ServiceConfigurationEntity serviceConfigurationEntity =
                converter.getInitialServiceConfiguration(serviceDeploymentEntity);

        assertNotNull(serviceConfigurationEntity);
        assertEquals(
                serviceDeploymentEntity, serviceConfigurationEntity.getServiceDeploymentEntity());
        assertNotNull(serviceConfigurationEntity.getCreatedTime());
        assertNotNull(serviceConfigurationEntity.getConfiguration());
    }
}
