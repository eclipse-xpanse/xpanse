/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BuilderFactoryTest {

    @Test
    public void basicBuilderTest() {
        BuilderFactory builderFactory = new BuilderFactory();
        AtomBuilder builder = builderFactory.createBuilder(BuilderFactory.BASIC_BUILDER, new Ocl());

        Assertions.assertNotNull(builder);
    }

    @Test
    public void unsupportedBuilderTest() {
        BuilderFactory builderFactory = new BuilderFactory();

        Assertions.assertThrows(Exception.class,
                () -> builderFactory.createBuilder("invalid", new Ocl()));
    }
}
