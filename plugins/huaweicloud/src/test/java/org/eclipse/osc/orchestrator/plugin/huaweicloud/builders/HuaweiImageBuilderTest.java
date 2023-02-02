/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.BaseImage;
import org.eclipse.osc.modules.ocl.loader.data.models.Image;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Provisioner;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform.TerraformExecutor;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {Environment.class})
public class HuaweiImageBuilderTest {

    @Autowired
    Environment environment;

    private void prepareImage(Ocl ocl) {
        BaseImage baseImage = new BaseImage();
        baseImage.setName("Ubuntu 20.04 server 64bit");
        baseImage.setType("t2.large");
        List<BaseImage> baseImageList = new ArrayList<>();
        baseImageList.add(baseImage);

        Artifact artifact = new Artifact();
        artifact.setName("kafka_image");
        artifact.setBase("$.image.base[0]");
        artifact.setProvisioners(Arrays.asList("$.image.provisioners[0]"));
        List<Artifact> artifactList = new ArrayList<>();
        artifactList.add(artifact);

        Provisioner provisioner = new Provisioner();
        provisioner.setName("my-kafka-release");
        provisioner.setType("shell");
        provisioner.setEnvironments(Arrays.asList("WORK_HOME=/usr/KAFKA"));
        provisioner.setInline(Arrays.asList(
                "echo \"start install docker\"",
                "echo \"run install\"",
                "apt-get update",
                "apt install -y docker.io",
                "docker network create app-tier --driver bridge",
                "docker run -d --name zookeeper-server --network app-tier -e "
                        + "ALLOW_ANONYMOUS_LOGIN=yes bitnami/zookeeper:latest",
                "docker run -d --name kafka-server --network app-tier -e ALLOW_PLAINTEXT_LISTENER=yes"
                        +
                        " -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181 bitnami/kafka:latest",
                "echo \"docker restart zookeeper-server\" >> /etc/profile",
                "echo \"docker restart kafka-server\" >> /etc/profile"
        ));
        List<Provisioner> provisionerList = new ArrayList<>();
        provisionerList.add(provisioner);

        Image image = new Image();
        image.setArtifacts(artifactList);
        image.setProvisioners(provisionerList);
        image.setBase(baseImageList);

        ocl.setImage(image);
    }

    private Ocl prepareOcl() {
        Ocl ocl = new Ocl();
        prepareImage(ocl);
        return ocl;
    }

    @Test
    public void builderTest() {
        HuaweiImageBuilder builder = new HuaweiImageBuilder(null);
        Assertions.assertThrows(BuilderException.class, () -> builder.create(null));
    }


    @Disabled
    @Test
    public void imageEnvTest() {
        Ocl ocl = prepareOcl();
        // Create builder context & ConfigService
        BuilderContext builderContext = new BuilderContext();
        builderContext.setEnvironment(this.environment);
        // Create HuaweiImageBuilder
        HuaweiImageBuilder imageBuilder = new HuaweiImageBuilder(ocl);
        builderContext.put(new HuaweiEnvBuilder(ocl).name(), new HashMap<>());
        TerraformExecutor tfExecutor = imageBuilder.prepareExecutor(builderContext);
        // Prepare the image described by Ocl.
        imageBuilder.prepareEnv(tfExecutor);
    }

    @Disabled
    @Test
    public void imageBuilderTest() {
        Ocl ocl = prepareOcl();
        // Create builder context & ConfigService
        BuilderContext builderContext = new BuilderContext();
        builderContext.setEnvironment(this.environment);
        // Create HuaweiImageBuilder
        HuaweiImageBuilder imageBuilder = new HuaweiImageBuilder(ocl);
        builderContext.put(new HuaweiEnvBuilder(ocl).name(), new HashMap<>());
        // Create the image described by Ocl.
        imageBuilder.create(builderContext);
    }
}
