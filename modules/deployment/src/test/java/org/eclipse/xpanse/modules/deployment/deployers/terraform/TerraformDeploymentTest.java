/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
        TerraformVersionProvider.class})
public class TerraformDeploymentTest {

    @Autowired
    TerraformDeployment terraformDeployment;

    @Autowired
    TerraformVersionProvider terraformVersionProvider;

    @MockBean
    DeployEnvironments deployEnvironments;

    @Disabled
    @Test
    public void basicTest() throws Exception {

        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));

        CreateRequest deployRequest = new CreateRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setCustomerServiceName("test");
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setVersion(ocl.getVersion());
        deployRequest.setFlavor(ocl.getFlavors().get(0).getName());

        Map<String, String> property = new HashMap<>();
        property.put("secgroup_id", "1234567890");
        deployRequest.setServiceRequestProperties(property);

        DeployTask xpanseDeployTask = new DeployTask();
        xpanseDeployTask.setId(UUID.randomUUID());
        xpanseDeployTask.setOcl(ocl);
        xpanseDeployTask.setDeployResourceHandler(null);
        xpanseDeployTask.setCreateRequest(deployRequest);
        TerraformDeployment terraformDeployment =
                new TerraformDeployment("test", false, "DEBUG", new DeployEnvironments(null,
                        null), terraformVersionProvider);

        DeployResult deployResult = terraformDeployment.deploy(xpanseDeployTask);

        Assertions.assertNotNull(deployResult);

    }

    @Test
    public void throwExceptionWhenDestroyFails() {
        CreateRequest createRequest =
                Instancio.of(CreateRequest.class).set(field(CreateRequest::getCsp),
                        Csp.OPENSTACK).create();
        DeployTask deployTask = Instancio.of(DeployTask.class)
                .set(field(DeployTask::getCreateRequest), createRequest).create();
        when(this.deployEnvironments.getFlavorVariables(any(DeployTask.class))).thenReturn(
                new HashMap<>());
        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.destroy(deployTask, "test"));
    }

    @Test
    public void throwExceptionWhenDeployFails() {
        UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");

        org.eclipse.xpanse.modules.models.service.register.Deployment deployment =
                new org.eclipse.xpanse.modules.models.service.register.Deployment();
        deployment.setKind(DeployerKind.TERRAFORM);
        deployment.setDeployer("deployer");

        Ocl ocl = new Ocl();
        ocl.setName("oclName");
        ocl.setDeployment(deployment);

        CreateRequest createRequest = new CreateRequest();
        createRequest.setId(uuid);
        createRequest.setUserName("userName");
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

        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.deploy(deployTask));
    }

    @Test
    void testGetDeployerKind() {
        DeployerKind deployerKind = terraformDeployment.getDeployerKind();

        Assertions.assertEquals(DeployerKind.TERRAFORM, deployerKind);
    }

}
