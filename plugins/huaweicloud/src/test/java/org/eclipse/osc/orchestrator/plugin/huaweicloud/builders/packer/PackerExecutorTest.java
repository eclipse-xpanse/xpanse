/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.BaseImage;
import org.eclipse.osc.modules.ocl.loader.data.models.Image;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Provisioner;
import org.eclipse.osc.modules.ocl.loader.data.models.Storage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PackerExecutorTest {

    private void prepareStorage(Ocl ocl) {
        // Storage
        Storage storage = new Storage();
        storage.setName("my-storage");
        storage.setType("ssd");
        storage.setSize("80GiB");

        // Storage list
        List<Storage> storageList = new ArrayList<>();
        storageList.add(storage);

        ocl.setStorage(storageList);
    }

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
        prepareStorage(ocl);
        prepareImage(ocl);

        return ocl;
    }

    @Disabled
    @Test
    public void packeExecutorBasicTest() {
        Ocl ocl = prepareOcl();

        PackerExecutor packerExecutor = new PackerExecutor(ocl,
                ocl.getImage().getArtifacts().get(0), null);

        packerExecutor.createWorkspace();
        packerExecutor.createInstallScript();
        packerExecutor.createPackerScript(null);

        packerExecutor.packerInit();
        packerExecutor.packerBuild();
    }

}
