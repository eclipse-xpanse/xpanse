package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfState;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class HuaweiTerraformResourceHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final HuaweiTerraformResourceHandler handler = new HuaweiTerraformResourceHandler();

    @Test
    public void handlerTest() throws IOException {
        TfState tfState = mapper.readValue(new File("target/test-classes/tfstate.json"),
                TfState.class);
        DeployResult deployResult = new DeployResult();
        deployResult.getProperty().put("stateFile", mapper.writeValueAsString(tfState));
        handler.handler(deployResult);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(deployResult.getResources()));
    }

}

