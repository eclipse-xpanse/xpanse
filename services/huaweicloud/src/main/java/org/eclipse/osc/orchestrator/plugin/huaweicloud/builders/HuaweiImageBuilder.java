package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer.PackerExecutor;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class HuaweiImageBuilder extends AtomBuilder {
    public HuaweiImageBuilder(Ocl ocl) {
        super(ocl);
    }

    @Override
    public String name() {
        return "Huawei-Cloud-image-Builder";
    }

    @Override
    public boolean create(BuilderContext ctx) {
        log.info("Creating Huawei Cloud Image.");
        if (ctx == null) {
            log.error("Dependent builder: {} must build first.", new HuaweiEnvBuilder(ocl).name());
            throw new BuilderException(this, "Builder context is null.");
        }

        if (ocl == null || ocl.getImage() == null || ocl.getImage().getArtifacts() == null) {
            log.info("There's no image artifacts need to be built.");
            return true;
        }

        String imageId = "";
        Map<String, String> imageCtx = new HashMap<>();

        for (var artifact : ocl.getImage().getArtifacts()) {
            PackerExecutor packerExecutor =
                new PackerExecutor(ocl, artifact, ctx.get(new HuaweiEnvBuilder(ocl).name()));

            packerExecutor.createWorkspace();
            packerExecutor.createInstallScript();
            packerExecutor.createPackerScript();

            imageId = packerExecutor.packerBuild();

            if (!packerExecutor.packerInit()) {
                throw new BuilderException(this, "PackerExecutor.packerInit failed." + name());
            }
            if (imageId.isEmpty()) {
                throw new BuilderException(
                    this, "PackerExecutor.packerBuild failed: imageId not found." + name());
            } else {
                imageCtx.put(artifact.getName(), imageId);
            }
        }

        ctx.put(name(), imageCtx);
        return true;
    }

    @Override
    public boolean destroy(BuilderContext ctx) {
        log.info("Destroying Huawei Cloud Image.");
        // TODO: destroy the temporary images here or after resources created.
        return true;
    }
}
