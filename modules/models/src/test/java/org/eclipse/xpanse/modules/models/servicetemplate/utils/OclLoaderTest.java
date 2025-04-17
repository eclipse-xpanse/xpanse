/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.utils;

import java.io.File;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.servicetemplate.HelmDeployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.TerraformDeployment;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test of OclLoader. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OclLoader.class})
public class OclLoaderTest {

    @Autowired OclLoader oclLoader;

    @Test
    public void testGetOclWithTerraformDeployment() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(
                        new File("src/test/resources/ocl_terraform_test.yml").toURI().toURL());
        Assertions.assertNotNull(ocl);
        Assertions.assertEquals(Category.OTHERS, ocl.getCategory());
        Assertions.assertEquals("terraform-test", ocl.getName());
        Assertions.assertInstanceOf(
                TerraformDeployment.class, ocl.getDeployment().getTerraformDeployment());
        Assertions.assertEquals(
                DeployerKind.TERRAFORM, ocl.getDeployment().getDeployerTool().getKind());
    }

    @Test
    public void testGetOclWithHelmDeployment() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(new File("src/test/resources/ocl_helm_test.yml").toURI().toURL());
        Assertions.assertNotNull(ocl);
        Assertions.assertEquals(Category.OTHERS, ocl.getCategory());
        Assertions.assertEquals("helm-test", ocl.getName());
        Assertions.assertInstanceOf(HelmDeployment.class, ocl.getDeployment().getHelmDeployment());
        Assertions.assertEquals(DeployerKind.HELM, ocl.getDeployment().getDeployerTool().getKind());
    }
}
