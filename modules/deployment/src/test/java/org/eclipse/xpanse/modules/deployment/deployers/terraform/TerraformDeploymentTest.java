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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for TerraformDeploy.
 */
@Slf4j
public class TerraformDeploymentTest {

    private final TerraformDeployment terraformDeployment = new TerraformDeployment();

    @Test
    public void deploy_test() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));

        CreateRequest createRequest = new CreateRequest();
        createRequest.setName(ocl.getName());
        createRequest.setCsp(ocl.getCloudServiceProvider().getName());
        createRequest.setVersion(ocl.getVersion());
        createRequest.setFlavor(ocl.getFlavors().get(0).getName());
        createRequest.setRegion(ocl.getCloudServiceProvider().getRegions().get(0));

        Map<String, String> property = new HashMap<>();
        // The secgroup_id use already exists.
        property.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setProperty(property);

        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployTask.setCreateRequest(createRequest);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);

        DeployResult deployResult = terraformDeployment.deploy(deployTask);
        log.error(deployResult.getProperty().get("stateFile"));
        Assertions.assertEquals(TerraformExecState.DEPLOY_SUCCESS, deployResult.getState());
    }

    @Test
    public void destroy_test() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));

        CreateRequest createRequest = new CreateRequest();
        createRequest.setName(ocl.getName());
        createRequest.setCsp(ocl.getCloudServiceProvider().getName());
        createRequest.setVersion(ocl.getVersion());
        createRequest.setFlavor(ocl.getFlavors().get(0).getName());
        createRequest.setRegion(ocl.getCloudServiceProvider().getRegions().get(0));

        Map<String, String> property = new HashMap<>();
        // The secgroup_id usage already exists.
        property.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setProperty(property);

        DeployTask deployTask = new DeployTask();
        // The tastId use the resource ID that has been successfully deployed.
        deployTask.setId(UUID.fromString("c2366707-1844-4234-b27a-c5a96dc56617"));
        deployTask.setCreateRequest(createRequest);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);

        DeployResult deployResult = terraformDeployment.destroy(deployTask);
        Assertions.assertEquals(TerraformExecState.DESTROY_SUCCESS, deployResult.getState());
    }

    @Test
    public void getDeployerKind_test() {
        DeployerKind deployerKind = terraformDeployment.getDeployerKind();
        Assertions.assertEquals(DeployerKind.TERRAFORM, deployerKind);
    }

}
