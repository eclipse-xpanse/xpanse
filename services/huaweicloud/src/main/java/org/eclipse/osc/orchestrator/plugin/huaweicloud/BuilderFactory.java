package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.Optional;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiEnvBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiImageBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.HuaweiResourceBuilder;
import org.eclipse.osc.services.ocl.loader.Ocl;

public class BuilderFactory {

    public Optional<AtomBuilder> createBuilder(String builderType, Ocl ocl) {
        if (builderType.equals("basic")) {
            HuaweiEnvBuilder envBuilder = new HuaweiEnvBuilder(ocl);
            HuaweiImageBuilder imageBuilder = new HuaweiImageBuilder(ocl);
            HuaweiResourceBuilder resourceBuilder = new HuaweiResourceBuilder(ocl);

            imageBuilder.addSubBuilder(envBuilder);
            resourceBuilder.addSubBuilder(imageBuilder);

            return Optional.of(resourceBuilder);
        }
        return Optional.empty();
    }
}
