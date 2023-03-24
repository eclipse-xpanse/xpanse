/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.providers;


import org.eclipse.xpanse.modules.deployment.deployers.terraform.Provider;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * Terraform provider information for AWS.
 */
public class Aws implements Provider {

    public static final String PROVIDER = """
            terraform {
              required_providers {
                aws = {
                  source  = "hashicorp/aws"
                  version = "~> 4.0"
                }
              }
            }
            
            provider "aws" {
              region = "%s"
            }
            """;

    @Override
    public String getProvider(String region) {
        return String.format(PROVIDER, region);
    }

    @Override
    public Csp getCsp() {
        return Csp.AWS;
    }

}


