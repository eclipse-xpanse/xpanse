/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformExecutorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Version configuration classes for Terraform cloud providers.
 */
@Configuration
public class TerraformProviderVersion {

    @Value("${terraform.provider.huaweicloud.version}")
    private String terraformHuaweiCloudVersion;

    @Value("${terraform.provider.flexibleengine.version}")
    private String terraformFlexibleEngineVersion;

    @Value("${terraform.provider.openstack.version}")
    private String terraformOpenStackVersion;

    @Value("${terraform.provider.aws.version}")
    private String terraformAwsCloudVersion;

    @Value("${terraform.provider.scs.version}")
    private String terraformScsVersion;

    /**
     * get the Terraform version corresponding to the cloud provider.
     */
    public String getTerraformVersionByCsp(Csp csp) {
        if (csp == Csp.HUAWEI) {
            return terraformHuaweiCloudVersion;
        } else if (csp == Csp.FLEXIBLE_ENGINE) {
            return terraformFlexibleEngineVersion;
        } else if (csp == Csp.OPENSTACK) {
            return terraformOpenStackVersion;
        } else if (csp == Csp.AWS) {
            return terraformAwsCloudVersion;
        } else if (csp == Csp.SCS) {
            return terraformScsVersion;
        } else {
            throw new TerraformExecutorException(
                    String.format("Terraform provider version for CSP %s is not configured",
                            csp.toValue()));
        }
    }
}
