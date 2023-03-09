/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for TerraformDeploy.
 */
public class TerraformDeploymentTest {

    @Disabled
    @Test
    public void basicTest() throws Exception {

        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));

        CreateRequest deployRequest = new CreateRequest();
        deployRequest.setName(ocl.getName());
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setVersion(ocl.getVersion());
        deployRequest.setFlavor(ocl.getFlavors().get(0).getName());

        Map<String, String> property = new HashMap<>();
        property.put("secgroup_id", "1234567890");
        deployRequest.setProperty(property);

        DeployTask xpanseDeployTask = new DeployTask();
        xpanseDeployTask.setId(UUID.randomUUID());
        xpanseDeployTask.setOcl(ocl);
        xpanseDeployTask.setDeployResourceHandler(null);
        xpanseDeployTask.setCreateRequest(deployRequest);
        TerraformDeployment terraformDeployment = new TerraformDeployment();

        DeployResult deployResult = terraformDeployment.deploy(xpanseDeployTask);

        Assertions.assertNotNull(deployResult);

    }
}
