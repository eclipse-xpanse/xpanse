package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import static org.mockito.Mockito.*;

import java.util.stream.Stream;
import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiEnvBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiImageBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiResourceBuilder;
import org.eclipse.osc.services.ocl.loader.Ocl;
import org.eclipse.osc.services.ocl.loader.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HuaweiCloudTest {
    private HuaweiEnvBuilder envBuilder;
    private HuaweiImageBuilder imageBuilder;
    private HuaweiResourceBuilder resourceBuilder;
    private BuilderContext ctx;
    private Ocl ocl;

    @BeforeEach
    public void mockBuilder() {
        Ocl ocl = new Ocl();
        ctx = new BuilderContext();

        envBuilder = mock(HuaweiEnvBuilder.class,
            withSettings().useConstructor(ocl).defaultAnswer(CALLS_REAL_METHODS));
        imageBuilder = mock(HuaweiImageBuilder.class,
            withSettings().useConstructor(ocl).defaultAnswer(CALLS_REAL_METHODS));
        resourceBuilder = mock(HuaweiResourceBuilder.class,
            withSettings().useConstructor(ocl).defaultAnswer(CALLS_REAL_METHODS));
    }

    @Test
    public void loadPluginTest() throws Exception {
        Minho karaf =
            Minho.builder()
                .loader(()
                            -> Stream.of(new LifeCycleService(), new OclLoader(),
                                new OrchestratorService(), new HuaweiCloudOrchestratorPlugin()))
                .build()
                .start();

        ServiceRegistry serviceRegistry = karaf.getServiceRegistry();
        OrchestratorService orchestratorService = serviceRegistry.get(OrchestratorService.class);

        Assertions.assertEquals(1, orchestratorService.getPlugins().size());
        Assertions.assertTrue(
            orchestratorService.getPlugins().get(0) instanceof HuaweiCloudOrchestratorPlugin);

        orchestratorService.registerManagedService("file:./target/test-classes/huawei_test.json");

        orchestratorService.startManagedService("my-service");
    }

    @Test
    public void builderListTest() {
        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        BuilderContext ctx = new BuilderContext();

        when(envBuilder.create(any())).thenReturn(true);
        when(imageBuilder.create(any())).thenReturn(true);
        when(resourceBuilder.create(any())).thenReturn(true);
        Assertions.assertTrue(resourceBuilder.build(ctx));

        verify(envBuilder, times(1)).build(ctx);
        verify(envBuilder, times(1)).create(ctx);
    }

    @Test
    public void builderTreeTest() {
        HuaweiEnvBuilder envBuilder2 = mock(HuaweiEnvBuilder.class,
            withSettings().useConstructor(ocl).defaultAnswer(CALLS_REAL_METHODS));

        imageBuilder.addSubBuilder(envBuilder);
        imageBuilder.addSubBuilder(envBuilder2);
        resourceBuilder.addSubBuilder(imageBuilder);

        when(envBuilder.create(any())).thenReturn(true);
        when(imageBuilder.create(any())).thenReturn(true);
        when(resourceBuilder.create(any())).thenReturn(true);
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

        BuilderContext ctx = new BuilderContext();

        when(envBuilder.create(any())).thenReturn(false);
        when(imageBuilder.create(any())).thenReturn(true);
        when(resourceBuilder.create(any())).thenReturn(true);

        Assertions.assertFalse(resourceBuilder.build(ctx));

        verify(envBuilder, times(1)).build(ctx);
        verify(envBuilder, times(1)).create(ctx);
    }

    @Test
    public void builderListRollbackTest() {
        imageBuilder.addSubBuilder(envBuilder);
        resourceBuilder.addSubBuilder(imageBuilder);

        when(envBuilder.destroy(any())).thenReturn(true);
        when(imageBuilder.destroy(any())).thenReturn(true);
        when(resourceBuilder.destroy(any())).thenReturn(true);
        Assertions.assertTrue(resourceBuilder.rollback(ctx));

        verify(envBuilder, times(1)).rollback(ctx);
        verify(envBuilder, times(1)).destroy(ctx);
    }

    @Test
    public void builderTreeRollbackTest() {
        HuaweiEnvBuilder envBuilder2 = mock(HuaweiEnvBuilder.class,
            withSettings().useConstructor(ocl).defaultAnswer(CALLS_REAL_METHODS));

        imageBuilder.addSubBuilder(envBuilder);
        imageBuilder.addSubBuilder(envBuilder2);
        resourceBuilder.addSubBuilder(imageBuilder);

        when(envBuilder.destroy(any())).thenReturn(true);
        when(imageBuilder.destroy(any())).thenReturn(true);
        when(resourceBuilder.destroy(any())).thenReturn(true);
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

        when(envBuilder.destroy(any())).thenReturn(false);
        when(imageBuilder.destroy(any())).thenReturn(true);
        when(resourceBuilder.destroy(any())).thenReturn(true);

        Assertions.assertFalse(resourceBuilder.rollback(ctx));

        verify(envBuilder, times(1)).rollback(ctx);
        verify(envBuilder, times(1)).destroy(ctx);
    }
}
