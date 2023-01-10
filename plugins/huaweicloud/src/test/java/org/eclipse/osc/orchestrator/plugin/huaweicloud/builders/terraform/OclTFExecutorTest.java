package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.nio.file.Files;
import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.modules.ocl.loader.OclResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OclTFExecutorTest {

    @Test
    public void TFExecutorBasicTest() throws Exception {
        OclLoader oclLoader = new OclLoader();
        oclLoader.onRegister(null);
        Ocl ocl = oclLoader.getOcl(
            new File("target/test-classes/huawei_test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        OclTFExecutor oclTFExecutor = spy(new OclTFExecutor(ocl, null));

        String content =
            Files.readString(new File("target/test-classes/tfstate.json").toPath());
        doReturn(content).when(oclTFExecutor).getTFState();

        OclResources oclResources = new OclResources();
        oclTFExecutor.updateOclResources(oclResources);

        Assertions.assertEquals(16, oclResources.getResources().size());
    }
}
