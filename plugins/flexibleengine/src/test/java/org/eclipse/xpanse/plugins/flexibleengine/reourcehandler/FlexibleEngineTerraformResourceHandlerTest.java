package org.eclipse.xpanse.plugins.flexibleengine.reourcehandler;

import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment.STATE_FILE_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfState;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.plugins.flexibleengine.resourcehandler.FlexibleEngineTerraformResourceHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FlexibleEngineTerraformResourceHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final FlexibleEngineTerraformResourceHandler flexibleHandler =
            new FlexibleEngineTerraformResourceHandler();

    @Test
    void handler() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/flexible-tfstate.json").toURL(), TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getDeploymentGeneratedFiles()
                .put(STATE_FILE_NAME, objectMapper.writeValueAsString(tfState));
        flexibleHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(deployResult.getResources()));
        Assertions.assertFalse(deployResult.getOutputProperties().isEmpty());
    }


    @Test
    void handler_destroy() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/flexible-tfstate-destroy.json").toURL(),
                TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getDeploymentGeneratedFiles().put(STATE_FILE_NAME,
                objectMapper.writeValueAsString(tfState));
        flexibleHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isEmpty(deployResult.getResources()));
        Assertions.assertTrue(deployResult.getOutputProperties().isEmpty());
    }
}
