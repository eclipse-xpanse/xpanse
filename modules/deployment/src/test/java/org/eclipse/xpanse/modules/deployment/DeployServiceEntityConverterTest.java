package org.eclipse.xpanse.modules.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DeployServiceEntityConverterTest {

    @Mock
    private ServiceTemplateStorage serviceTemplateStorage;

    @InjectMocks
    private DeployServiceEntityConverter converter;

    private DeployServiceEntity deployServiceEntity;
    private ServiceTemplateEntity serviceTemplateEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setId(UUID.randomUUID());
        deployServiceEntity.setDeployRequest(new DeployRequest());
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());

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
        ocl.setConfigurationParameters(new ArrayList<>());

        serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(deployServiceEntity.getServiceTemplateId());
        serviceTemplateEntity.setOcl(ocl);

        when(serviceTemplateStorage.getServiceTemplateById(
                deployServiceEntity.getServiceTemplateId()))
                .thenReturn(serviceTemplateEntity);
    }

    @Test
    void testGetDeployTaskByStoredService() {
        DeployTask deployTask = converter.getDeployTaskByStoredService(deployServiceEntity);

        assertNotNull(deployTask);
        assertEquals(deployServiceEntity.getId(), deployTask.getId());
        assertEquals(deployServiceEntity.getDeployRequest(), deployTask.getDeployRequest());
        assertEquals(serviceTemplateEntity.getOcl(), deployTask.getOcl());
        assertEquals(serviceTemplateEntity.getId(), deployTask.getServiceTemplateId());
    }

    @Test
    void testGetInitialServiceConfiguration() {
        ServiceConfigurationEntity serviceConfigurationEntity =
                converter.getInitialServiceConfiguration(deployServiceEntity);

        assertNotNull(serviceConfigurationEntity);
        assertEquals(deployServiceEntity, serviceConfigurationEntity.getDeployServiceEntity());
        assertNotNull(serviceConfigurationEntity.getCreatedTime());
        assertNotNull(serviceConfigurationEntity.getConfiguration());
    }

}
