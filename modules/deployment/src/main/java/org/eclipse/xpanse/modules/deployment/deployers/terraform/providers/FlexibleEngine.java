/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.providers;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.Provider;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;

/**
 * Terraform provider information for Huawei.
 */
public class FlexibleEngine implements Provider {

    public static final String PROVIDER = """         
            terraform {
              required_providers {
                flexibleengine = {
                  source  = "FlexibleEngineCloud/flexibleengine"
                  version = "%s"
                }
              }
            }
                        
            provider "flexibleengine" {
              region = "%s"
            }
            """;

    @Override
    public String getProvider(String version, String region) {
        return String.format(PROVIDER, version, region);
    }

    @Override
    public Csp getCsp() {
        return Csp.FLEXIBLE_ENGINE;
    }

}
