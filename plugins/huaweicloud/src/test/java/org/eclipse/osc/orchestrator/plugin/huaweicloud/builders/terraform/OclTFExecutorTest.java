package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.nio.file.Files;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OclLoader.class})
public class OclTFExecutorTest {

    @Autowired
    OclLoader oclLoader;
    @Test
    public void TFExecutorBasicTest() throws Exception {
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
