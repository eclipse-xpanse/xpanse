package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.eclipse.osc.modules.ocl.loader.OclResource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TFResourcesTest {

    @Disabled
    @Test
    public void TFExecutorBasicTest() throws Exception {
        String content =
            Files.readString(new File("target/test-classes/tfstate.json").toPath());
        ObjectMapper objectMapper = new ObjectMapper();
        TFState tfState = objectMapper.readValue(content, TFState.class);

        TFResources tfResources = new TFResources();
        tfResources.update(tfState);
        List<OclResource> oclResourceList = tfResources.getResources();
    }
}
