package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class BuilderFactoryTest {

    @Test
    public void basicBuilderTest() {
        BuilderFactory builderFactory = new BuilderFactory();

        Optional<AtomBuilder> builder = builderFactory.createBuilder(BuilderFactory.BASIC_BUILDER,
            new Ocl());

        Assertions.assertTrue(builder.isPresent());
    }

    @Test
    public void unsupportedBuilderTest() {
        BuilderFactory builderFactory = new BuilderFactory();

        Optional<AtomBuilder> builder = builderFactory.createBuilder("invalid", new Ocl());

        Assertions.assertTrue(builder.isEmpty());
    }
}
