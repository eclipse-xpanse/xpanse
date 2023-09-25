/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for TerraformDeploy.
 */

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {TerraformDeployment.class, DeployEnvironments.class,
        PluginManager.class, DeployServiceStorage.class, DeployResourceStorage.class,
        TerraformLocalConfig.class})
class TerraformDeploymentTest {

    @Autowired
    TerraformDeployment terraformDeployment;

    @MockBean
    DeployEnvironments deployEnvironments;

    @MockBean
    DeployServiceStorage deployServiceStorage;

    @MockBean
    DeployResourceStorage deployResourceStorage;

    @MockBean
    TerraformLocalConfig terraformLocalConfig;

    @MockBean
    PluginManager pluginManager;

    @Test
    void basicTest() throws Exception {

        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test.yaml"));

        CreateRequest deployRequest = new CreateRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setCustomerServiceName("test");
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().get(0).getName());

        Map<String, String> property = new HashMap<>();
        property.put("secgroup_id", "1234567890");
        deployRequest.setServiceRequestProperties(property);

        DeployTask xpanseDeployTask = new DeployTask();
        xpanseDeployTask.setId(UUID.randomUUID());
        xpanseDeployTask.setOcl(ocl);
        xpanseDeployTask.setDeployResourceHandler(null);
        xpanseDeployTask.setCreateRequest(deployRequest);
        doReturn(new HashMap<>()).when(this.deployEnvironments).getCredentialVariables(any(DeployTask.class));
        doReturn("""
            terraform {
              required_providers {
                openstack = {
                      source  = "terraform-provider-openstack/openstack"
                      version = ">= 1.48.0"
                    }
              }
            }
                        
            provider "openstack" {
              region = "test"
            }
            """).when(this.pluginManager).getTerraformProviderForRegionByCsp(any(Csp.class), any());
        Assertions.assertThrows(ServiceNotDeployedException.class, ()->{
                    terraformDeployment.deploy(xpanseDeployTask);
                });

    }

    @Test
    void throwExceptionWhenDestroyFails() {
        CreateRequest createRequest =
                Instancio.of(CreateRequest.class).set(field(CreateRequest::getCsp),
                        Csp.OPENSTACK).create();
        DeployTask deployTask = Instancio.of(DeployTask.class)
                .set(field(DeployTask::getCreateRequest), createRequest).create();
        when(this.deployEnvironments.getFlavorVariables(any(DeployTask.class))).thenReturn(
                new HashMap<>());
        doReturn("""
            terraform {
              required_providers {
                openstack = {
                      source  = "terraform-provider-openstack/openstack"
                      version = ">= 1.48.0"
                    }
              }
            }
                        
            provider "openstack" {
              region = "test"
            }
            """).when(this.pluginManager).getTerraformProviderForRegionByCsp(any(Csp.class), any());
        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.destroy(deployTask, "test"));
    }

    @Test
    void throwExceptionWhenDeployFails() {
        UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");

        Deployment deployment =
                new Deployment();
        deployment.setKind(DeployerKind.TERRAFORM);
        deployment.setDeployer("deployer");

        Ocl ocl = new Ocl();
        ocl.setName("oclName");
        ocl.setDeployment(deployment);

        CreateRequest createRequest = new CreateRequest();
        createRequest.setId(uuid);
        createRequest.setUserId("UserId");
        createRequest.setCategory(Category.COMPUTE);
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setServiceName("service");
        createRequest.setCustomerServiceName("customerService");
        createRequest.setVersion("1.0");
        createRequest.setOcl(ocl);
        createRequest.setFlavor("flavor");
        createRequest.setRegion("cn-north-1");

        DeployTask deployTask = new DeployTask();
        deployTask.setId(uuid);
        deployTask.setCreateRequest(createRequest);
        deployTask.setOcl(ocl);
        doReturn("""
            terraform {
              required_providers {
                openstack = {
                      source  = "terraform-provider-openstack/openstack"
                      version = ">= 1.48.0"
                    }
              }
            }
                        
            provider "openstack" {
              region = "test"
            }
            """).when(this.pluginManager).getTerraformProviderForRegionByCsp(any(Csp.class), any());
        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.deploy(deployTask));
    }

    @Test
    void testGetDeployerKind() {
        DeployerKind deployerKind = terraformDeployment.getDeployerKind();

        Assertions.assertEquals(DeployerKind.TERRAFORM, deployerKind);
    }

}
