/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.Provider;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Test;

/**
 * Test of Aws.
 */
class AwsTest {

    private static Provider awsProvider;

    @Test
    void setUp() {
        awsProvider = new Aws();
    }

    @Test
    public void testGetProvider() {
        String region = "us-west-2";

        String providerInfo = awsProvider.getProvider("~> 4.0", region);
        String expectedProviderInfo = """
                terraform {
                  required_providers {
                    aws = {
                      source  = "hashicorp/aws"
                      version = "~> 4.0"
                    }
                  }
                }
                            
                provider "aws" {
                  region = "us-west-2"
                }
                """;

        assertEquals(expectedProviderInfo, providerInfo);
    }

    @Test
    public void testGetCsp() {
        Csp csp = awsProvider.getCsp();

        assertEquals(Csp.AWS, csp);
    }

}
