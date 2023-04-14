package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfState;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlexibleEngineTerraformResourceHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final FlexibleEngineTerraformResourceHandler flexibleHandler =
            new FlexibleEngineTerraformResourceHandler();

    @Test
    void handler() throws IOException {
        TfState tfState = objectMapper.readValue(
                new URL("file:./target/test-classes/flexible-tfstate.json"), TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getPrivateProperties().put("stateFile",
                objectMapper.writeValueAsString(tfState));
        flexibleHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(deployResult.getResources()));
        Assertions.assertFalse(deployResult.getProperties().isEmpty());
    }


    @Test
    void handler_destroy() throws IOException {
        TfState tfState = objectMapper.readValue(
                new URL("file:./target/test-classes/flexible-tfstate-destroy.json"), TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getPrivateProperties().put("stateFile",
                objectMapper.writeValueAsString(tfState));
        flexibleHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isEmpty(deployResult.getResources()));
        Assertions.assertTrue(deployResult.getProperties().isEmpty());
    }
}