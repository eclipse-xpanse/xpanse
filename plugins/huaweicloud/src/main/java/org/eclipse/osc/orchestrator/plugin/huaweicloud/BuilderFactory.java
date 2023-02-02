/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.Optional;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiEnvBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiImageBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiResourceBuilder;

/**
 * Factory class to instantiate builder object.
 */
public class BuilderFactory {

    public static final String BASIC_BUILDER = "basic";

    /**
     * Factory method that instantiates complete builder object.
     *
     * @param builderType Type of the builder.
     * @param ocl         Complete Ocl descriptor of the managed service to be deployed.
     * @return AtomBuilder object.
     */
    public Optional<AtomBuilder> createBuilder(String builderType, Ocl ocl) {
        if (builderType.equals(BASIC_BUILDER)) {
            HuaweiEnvBuilder envBuilder = new HuaweiEnvBuilder(ocl);
            HuaweiImageBuilder imageBuilder = new HuaweiImageBuilder(ocl);
            HuaweiResourceBuilder resourceBuilder = new HuaweiResourceBuilder(ocl);
            HuaweiEnvBuilder envBuilderTail = new HuaweiEnvBuilder(ocl);

            imageBuilder.addSubBuilder(envBuilder);
            resourceBuilder.addSubBuilder(imageBuilder);
            envBuilderTail.addSubBuilder(resourceBuilder);

            return Optional.of(envBuilderTail);
        }
        return Optional.empty();
    }
}
