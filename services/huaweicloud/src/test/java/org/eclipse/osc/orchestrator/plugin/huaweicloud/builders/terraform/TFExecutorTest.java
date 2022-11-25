package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.io.File;
import java.util.stream.Stream;
import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiEnvBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiImageBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiResourceBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.TFExecutorException;
import org.eclipse.osc.services.ocl.loader.Ocl;
import org.eclipse.osc.services.ocl.loader.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TFExecutorTest {

    @Test
    public void TFExecutorBasicTest() throws Exception {
        Minho minho = Minho.builder().loader(() -> Stream.of(new OclLoader())).build().start();
        Ocl ocl = minho.getServiceRegistry()
            .get(OclLoader.class)
            .getOcl(new File("target/test-classes/huawei_test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        HuaweiEnvBuilder envBuilder;
        HuaweiImageBuilder imageBuilder;
        HuaweiResourceBuilder resourceBuilder;

        envBuilder = new HuaweiEnvBuilder(ocl);
        imageBuilder = new HuaweiImageBuilder(ocl);
        resourceBuilder = new HuaweiResourceBuilder(ocl);

        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        BuilderContext builderContext = new BuilderContext();
        ConfigService configService = new ConfigService();
        builderContext.setConfig(configService);

        for (var artifact : ocl.getImage().getArtifacts()) {
            artifact.setId("cecc4bcf-b055-4d35-bd5f-693d4412eaef");
        }
        Assertions.assertThrows(
            TFExecutorException.class, () -> resourceBuilder.build(builderContext));
    }

    @Test
    public void TFExecutorPlanRollBackTest() throws Exception {
        Minho minho = Minho.builder().loader(() -> Stream.of(new OclLoader())).build().start();
        Ocl ocl = minho.getServiceRegistry()
            .get(OclLoader.class)
            .getOcl(new File("target/test-classes/huawei_test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        HuaweiEnvBuilder envBuilder;
        HuaweiResourceBuilder resourceBuilder;

        envBuilder = new HuaweiEnvBuilder(ocl);
        resourceBuilder = new HuaweiResourceBuilder(ocl);

        BuilderContext builderContext = new BuilderContext();
        ConfigService configService = new ConfigService();
        builderContext.setConfig(configService);

        for (var artifact : ocl.getImage().getArtifacts()) {
            artifact.setId("cecc4bcf-b055-4d35-bd5f-693d4412eaef");
        }

        envBuilder.destroy(builderContext);
        Assertions.assertThrows(
            TFExecutorException.class, () -> resourceBuilder.destroy(builderContext));
    }
}
