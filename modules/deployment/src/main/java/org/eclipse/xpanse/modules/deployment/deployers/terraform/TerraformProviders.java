/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import java.util.Objects;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.Aws;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.FlexibleEngine;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.Huawei;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.Openstack;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.Scs;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformProviderNotFoundException;

/**
 * Terraform providers.
 */
public enum TerraformProviders {
    AWS(Csp.AWS, new Aws()),
    HUAWEI(Csp.HUAWEI, new Huawei()),
    FLEXIBLE_ENGINE(Csp.FLEXIBLE_ENGINE, new FlexibleEngine()),
    OPENSTACK(Csp.OPENSTACK, new Openstack()),
    SCS(Csp.SCS, new Scs());

    final Csp csp;

    final Provider provider;

    /**
     * Constructor for TerraformProviders.
     *
     * @param csp      the cloud service provider.
     * @param provider the provider for the CSP.
     */
    TerraformProviders(Csp csp, Provider provider) {
        this.csp = csp;
        this.provider = provider;
    }

    /**
     * Get the provider by the Csp.
     *
     * @param csp the cloud service provider.
     */
    public static Provider getProvider(Csp csp) {
        for (TerraformProviders provider : values()) {
            if (Objects.equals(csp, provider.csp)) {
                return provider.provider;
            }
        }

        throw new TerraformProviderNotFoundException("Cannot find provider " + csp + ".");
    }
}
