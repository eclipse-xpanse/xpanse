package org.eclipse.xpanse.plugins.huaweicloud.resourcehandler;

import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment.STATE_FILE_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfState;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

class HuaweiCloudTerraformResourceHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HuaweiCloudTerraformResourceHandler huaweiHandler =
            new HuaweiCloudTerraformResourceHandler();

    @Test
    void handler() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/huawei-tfstate.json").toURL(), TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getDeploymentGeneratedFiles()
                .put(STATE_FILE_NAME, objectMapper.writeValueAsString(tfState));
        huaweiHandler.handler(deployResult);
        Assertions.assertFalse(CollectionUtils.isEmpty(deployResult.getResources()));
        Assertions.assertFalse(deployResult.getOutputProperties().isEmpty());
    }


    @Test
    void handler_destroy() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/huawei-tfstate-destroy.json").toURL(),
                TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getDeploymentGeneratedFiles()
                .put(STATE_FILE_NAME, objectMapper.writeValueAsString(tfState));
        huaweiHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isEmpty(deployResult.getResources()));
        Assertions.assertTrue(deployResult.getOutputProperties().isEmpty());
    }
}
