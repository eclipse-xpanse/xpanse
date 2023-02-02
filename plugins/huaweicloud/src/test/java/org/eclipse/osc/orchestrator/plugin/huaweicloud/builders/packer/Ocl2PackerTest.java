/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.BaseImage;
import org.eclipse.osc.modules.ocl.loader.data.models.Image;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Provisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Ocl2PackerTest {

    private void prepareImage(Ocl ocl) {
        BaseImage baseImage = new BaseImage();
        baseImage.setName("ubuntu-x86");
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
        provisioner.setInline(Arrays.asList("cd ${WORK_HOME} && wget "
                + "http://xxxx/kafka/release.jar"));
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
    public void basicTest() {
        Ocl ocl = prepareOcl();
        Ocl2Packer ocl2Packer = new Ocl2Packer(ocl, ocl.getImage().getArtifacts().get(0));

        String hclImages = ocl2Packer.getHclImages(new PackerVars());

        Assertions.assertTrue(
                Pattern.compile("image_name.*=.*kafka_image").matcher(hclImages).find());
        Assertions.assertTrue(
                Pattern.compile("source.*\"huaweicloud-ecs\".*\"kafka_image\".*").matcher(hclImages)
                        .find());

        String installScript = ocl2Packer.getInstallScript();
        Assertions.assertTrue(
                Pattern.compile("cd \\$\\{WORK_HOME\\} && wget http://xxxx/kafka/release.jar")
                        .matcher(installScript)
                        .find());
    }
}
