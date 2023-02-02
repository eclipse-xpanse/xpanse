/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader;

import java.io.File;
import java.util.Optional;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.BaseImage;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Provisioner;
import org.eclipse.osc.modules.ocl.loader.data.models.Vm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OclLoader.class,})
public class OclLoaderTest {

    @Autowired
    OclLoader oclLoader;

    @Test
    public void loading() throws Exception {

        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        Assertions.assertEquals("flat", ocl.getBilling().getModel());
        Assertions.assertEquals("euro", ocl.getBilling().getCurrency());
        Assertions.assertEquals("monthly", ocl.getBilling().getPeriod());
        Assertions.assertEquals("instance", ocl.getBilling().getVariableItem());
        Assertions.assertEquals(20.0, ocl.getBilling().getFixedPrice());
        Assertions.assertEquals(10.0, ocl.getBilling().getVariablePrice());

        Assertions.assertEquals("ubuntu-x64", ocl.getImage().getBase().get(0).getName());
    }

    @Test
    public void testBlockAssociationProvisioner() throws Exception {
        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/test.json").toURI().toURL());
        Assertions.assertNotNull(ocl);
        String KafkaProvisionerStr = ocl.getImage().getArtifacts().get(0).getProvisioners().get(0);
        Optional<Provisioner> kafkaProvisioner = ocl.referTo(KafkaProvisionerStr,
                Provisioner.class);
        Assertions.assertEquals("my-kafka-release", kafkaProvisioner.get().getName());
        Assertions.assertEquals("shell", kafkaProvisioner.get().getType());
        Assertions.assertEquals("WORK_HOME=/usr1/KAFKA/",
                kafkaProvisioner.get().getEnvironments().get(0));
        Assertions.assertEquals("echo $PATH", kafkaProvisioner.get().getInline().get(1));
    }

    @Test
    public void testBlockAssociationArtifact() throws Exception {
        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/test.json").toURI().toURL());
        Assertions.assertNotNull(ocl);
        Vm vm = ocl.getCompute().getVm().get(0);
        Assertions.assertEquals("my-vm", vm.getName());
        Assertions.assertEquals("$.image.artifacts[0]", vm.getImage());

        Optional<Artifact> artifact = ocl.referTo(vm.getImage(), Artifact.class);
        Optional<BaseImage> baseImage = ocl.referTo(artifact.get().getBase(), BaseImage.class);
        Assertions.assertEquals("ubuntu-x64", baseImage.get().getName());
    }

    @Test
    public void testJsonParser() throws Exception {

        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        Optional<Artifact> artifact = ocl.referTo("$.xxxxxxxxxxxxxxxxxxxx", Artifact.class);
        Assertions.assertTrue(artifact.isEmpty());

        artifact = ocl.referTo("......xxxxxxxxxxxxxxxxxxxx", Artifact.class);
        Assertions.assertTrue(artifact.isEmpty());

        artifact = ocl.referTo("......xxxxx[1][3232]", Artifact.class);
        Assertions.assertTrue(artifact.isEmpty());

        Optional<Provisioner> provisioner = ocl.referTo("$.image.provisioners[1]",
                Provisioner.class);
        Assertions.assertFalse(provisioner.isPresent());

        provisioner = ocl.referTo("$.image.provisioners[0]", Provisioner.class);
        Assertions.assertTrue(provisioner.isPresent());
    }
}
