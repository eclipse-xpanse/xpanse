/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.providers;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.Provider;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;

/**
 * Terraform provider information for Openstack.
 */
public class Openstack implements Provider {

    public static final String PROVIDER = """
            terraform {
              required_providers {
                openstack = {
                      source  = "terraform-provider-openstack/openstack"
                      version = ">= 1.48.0"
                    }
              }
            }
                        
            provider "openstack" {
              region = "%s"
            }
            """;

    @Override
    public String getProvider(String region) {
        return String.format(PROVIDER, region);
    }

    @Override
    public Csp getCsp() {
        return Csp.OPENSTACK;
    }

}
