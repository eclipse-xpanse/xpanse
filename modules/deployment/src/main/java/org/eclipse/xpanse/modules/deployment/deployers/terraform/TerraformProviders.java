/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import java.util.Objects;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.Aws;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.Flexible;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.providers.Huawei;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * Terraform providers.
 */
public enum TerraformProviders {
    AWS(Csp.AWS, new Aws()),
    HUAWEI(Csp.HUAWEI, new Huawei()),
    FLEXIBLE(Csp.FLEXIBLE, new Flexible());

    Csp csp;

    Provider provider;

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

        throw new RuntimeException("Cannot find provider " + csp + ".");
    }
}
