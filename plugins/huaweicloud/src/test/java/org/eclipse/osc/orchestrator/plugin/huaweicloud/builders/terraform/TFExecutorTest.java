/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.io.File;
import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiEnvBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiImageBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiResourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OclLoader.class})
public class TFExecutorTest {

    @Autowired
    Environment environment;
    @Autowired
    OclLoader oclLoader;

    @Disabled
    @Test
    public void TFExecutorBasicTest() throws Exception {
        Ocl ocl = this.oclLoader
                .getOcl(new File("target/test-classes/huawei_test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        for (var artifact : ocl.getImage().getArtifacts()) {
            artifact.setId("cecc4bcf-b055-4d35-bd5f-693d4412eaef");
        }

        HuaweiEnvBuilder envBuilder;
        HuaweiImageBuilder imageBuilder;
        HuaweiResourceBuilder resourceBuilder;

        envBuilder = new HuaweiEnvBuilder(ocl);
        imageBuilder = new HuaweiImageBuilder(ocl);
        resourceBuilder = new HuaweiResourceBuilder(ocl);

        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        BuilderContext builderContext = new BuilderContext();
        builderContext.setEnvironment(environment);
    }

    @Disabled
    @Test
    public void TFExecutorPlanRollBackTest() throws Exception {
        Ocl ocl = this.oclLoader
                .getOcl(new File("target/test-classes/huawei_test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        for (var artifact : ocl.getImage().getArtifacts()) {
            artifact.setId("cecc4bcf-b055-4d35-bd5f-693d4412eaef");
        }

        HuaweiEnvBuilder envBuilder;
        HuaweiResourceBuilder resourceBuilder;

        envBuilder = new HuaweiEnvBuilder(ocl);
        resourceBuilder = new HuaweiResourceBuilder(ocl);

        BuilderContext builderContext = new BuilderContext();
        builderContext.setEnvironment(this.environment);

        envBuilder.destroy(builderContext);
        Assertions.assertThrows(
                IllegalStateException.class, () -> resourceBuilder.destroy(builderContext));
    }
}
