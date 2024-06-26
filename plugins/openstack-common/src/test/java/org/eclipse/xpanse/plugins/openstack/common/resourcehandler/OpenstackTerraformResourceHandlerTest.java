package org.eclipse.xpanse.plugins.openstack.common.resourcehandler;

import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment.STATE_FILE_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfState;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.plugins.openstack.common.resourcehandler.OpenstackTerraformResourceHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

class OpenstackTerraformResourceHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OpenstackTerraformResourceHandler openstackHandler =
            new OpenstackTerraformResourceHandler();

    @Test
    void handler() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/openstack-tfstate.json").toURL(),
                TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getPrivateProperties()
                .put(STATE_FILE_NAME, objectMapper.writeValueAsString(tfState));
        openstackHandler.handler(deployResult);
        Assertions.assertFalse(CollectionUtils.isEmpty(deployResult.getResources()));
        Assertions.assertFalse(deployResult.getProperties().isEmpty());
    }


    @Test
    void handler_destroy() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/openstack-tfstate-destroy.json").toURL(),
                TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getPrivateProperties()
                .put(STATE_FILE_NAME, objectMapper.writeValueAsString(tfState));
        openstackHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isEmpty(deployResult.getResources()));
        Assertions.assertTrue(deployResult.getProperties().isEmpty());
    }
}
