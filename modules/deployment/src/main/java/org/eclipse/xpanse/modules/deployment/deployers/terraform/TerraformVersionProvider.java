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
public class TerraformVersionProvider {

    @Value("${terraform.provider.huaweicloud.version}")
    private String terraformHuaweiCloudVersion;

    @Value("${terraform.provider.flexibleengine.version}")
    private String terraformFlexibleEngineVersion;

    @Value("${terraform.provider.openstack.version}")
    private String terraformOpenStackVersion;

    @Value("${terraform.provider.aws.version}")
    private String terraformAwsCloudVersion;

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
        } else {
            throw new TerraformExecutorException("Get Terraform Version,Csp does not exist");
        }
    }
}
