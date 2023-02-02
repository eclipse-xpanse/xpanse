/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiEnvBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiImageBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiResourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OclLoader.class})
public class AtomBuilderTest {

    @Autowired
    Environment environment;
    private HuaweiEnvBuilder envBuilder;
    private HuaweiImageBuilder imageBuilder;
    private HuaweiResourceBuilder resourceBuilder;
    private BuilderContext ctx;
    private Ocl ocl;

    @BeforeEach
    public void mockBuilder() {
        ocl = new Ocl();
        environment.getProperty(HuaweiEnvBuilder.ACCESS_KEY, "test_access_key");
        environment.getProperty(HuaweiEnvBuilder.SECRET_KEY, "test_secret_key");
        environment.getProperty(HuaweiEnvBuilder.REGION_NAME, "test_region_name");
        ctx = new BuilderContext();
        ctx.setEnvironment(environment);

        envBuilder = spy(new HuaweiEnvBuilder(ocl));
        imageBuilder = spy(new HuaweiImageBuilder(ocl));
        resourceBuilder = spy(new HuaweiResourceBuilder(ocl));
    }

    @Test
    public void builderListTest() {
        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        doReturn(true).when(envBuilder).create(any());
        doReturn(true).when(imageBuilder).create(any());
        doReturn(true).when(resourceBuilder).create(any());
        Assertions.assertTrue(resourceBuilder.build(ctx));

        verify(envBuilder, times(1)).build(ctx);
        verify(envBuilder, times(1)).create(ctx);
    }

    @Test
    public void builderTreeTest() {
        HuaweiEnvBuilder envBuilder2 = spy(new HuaweiEnvBuilder(ocl));

        imageBuilder.addSubBuilder(envBuilder);
        imageBuilder.addSubBuilder(envBuilder2);
        resourceBuilder.addSubBuilder(imageBuilder);

        doReturn(true).when(envBuilder).create(any());
        doReturn(true).when(imageBuilder).create(any());
        doReturn(true).when(resourceBuilder).create(any());
        Assertions.assertTrue(resourceBuilder.build(ctx));

        verify(envBuilder, times(1)).build(ctx);
        verify(envBuilder, times(1)).create(ctx);
        verify(envBuilder2, times(1)).build(ctx);
        verify(envBuilder2, times(1)).create(ctx);
        verify(imageBuilder, times(1)).create(ctx);
    }

    @Test
    public void builderListFailedTest() {
        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        doReturn(false).when(envBuilder).create(any());
        doReturn(true).when(imageBuilder).create(any());
        doReturn(true).when(resourceBuilder).create(any());

        Assertions.assertFalse(resourceBuilder.build(ctx));

        verify(envBuilder, times(1)).build(ctx);
        verify(envBuilder, times(1)).create(ctx);
    }

    @Test
    public void builderListRollbackTest() {
        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        doReturn(true).when(envBuilder).destroy(any());
        doReturn(true).when(imageBuilder).destroy(any());
        doReturn(true).when(resourceBuilder).destroy(any());
        Assertions.assertTrue(resourceBuilder.rollback(ctx));

        verify(envBuilder, times(1)).rollback(ctx);
        verify(envBuilder, times(1)).destroy(ctx);
    }

    @Test
    public void builderTreeRollbackTest() {
        HuaweiEnvBuilder envBuilder2 = spy(new HuaweiEnvBuilder(ocl));

        imageBuilder.addSubBuilder(envBuilder);
        imageBuilder.addSubBuilder(envBuilder2);
        resourceBuilder.addSubBuilder(imageBuilder);

        doReturn(true).when(envBuilder).destroy(any());
        doReturn(true).when(envBuilder2).destroy(any());
        doReturn(true).when(imageBuilder).destroy(any());
        doReturn(true).when(resourceBuilder).destroy(any());
        Assertions.assertTrue(resourceBuilder.rollback(ctx));

        verify(envBuilder, times(1)).rollback(ctx);
        verify(envBuilder, times(1)).destroy(ctx);
        verify(envBuilder2, times(1)).rollback(ctx);
        verify(envBuilder2, times(1)).destroy(ctx);
        verify(imageBuilder, times(1)).destroy(ctx);
    }

    @Test
    public void builderListRollbackFailedTest() {
        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        doReturn(false).when(envBuilder).destroy(any());
        doReturn(true).when(imageBuilder).destroy(any());
        doReturn(true).when(resourceBuilder).destroy(any());

        Assertions.assertFalse(resourceBuilder.rollback(ctx));

        verify(envBuilder, times(1)).rollback(ctx);
        verify(envBuilder, times(1)).destroy(ctx);
    }
}
