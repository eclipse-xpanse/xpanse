/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.builders.terraform;

import java.io.File;
import org.eclipse.xpanse.modules.ocl.loader.OclLoader;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.builders.HuaweiEnvBuilder;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.builders.HuaweiResourceBuilder;
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

        HuaweiResourceBuilder resourceBuilder = new HuaweiResourceBuilder(ocl);

        BuilderContext builderContext = new BuilderContext();
        builderContext.setEnvironment(environment);

        resourceBuilder.build(builderContext);
    }

    @Disabled
    @Test
    public void TFExecutorPlanRollBackTest() throws Exception {
        Ocl ocl = this.oclLoader
                .getOcl(new File("target/test-classes/huawei_test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

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
