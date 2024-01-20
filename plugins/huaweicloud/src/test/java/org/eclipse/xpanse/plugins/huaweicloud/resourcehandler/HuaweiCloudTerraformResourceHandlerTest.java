package org.eclipse.xpanse.plugins.huaweicloud.resourcehandler;

import static org.eclipse.xpanse.modules.deployment.deployers.terraform.TerraformDeployment.STATE_FILE_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfState;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HuaweiCloudTerraformResourceHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HuaweiCloudTerraformResourceHandler huaweiHandler =
            new HuaweiCloudTerraformResourceHandler();

    @Test
    void handler() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/huawei-tfstate.json").toURL(), TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getPrivateProperties()
                .put(STATE_FILE_NAME, objectMapper.writeValueAsString(tfState));
        huaweiHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(deployResult.getResources()));
        Assertions.assertFalse(deployResult.getProperties().isEmpty());
    }


    @Test
    void handler_destroy() throws IOException {
        TfState tfState = objectMapper.readValue(
                URI.create("file:src/test/resources/huawei-tfstate-destroy.json").toURL(),
                TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getPrivateProperties()
                .put(STATE_FILE_NAME, objectMapper.writeValueAsString(tfState));
        huaweiHandler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isEmpty(deployResult.getResources()));
        Assertions.assertTrue(deployResult.getProperties().isEmpty());
    }
}
