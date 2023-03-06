/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.terraform.deployment.providers;


import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Csp;
import org.eclipse.xpanse.modules.terraform.deployment.Provider;

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
            """;

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    public Csp getCsp() {
        return Csp.AWS;
    }

}


