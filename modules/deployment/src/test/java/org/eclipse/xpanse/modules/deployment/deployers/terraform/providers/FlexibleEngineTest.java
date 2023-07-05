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
 * Test of FlexibleEngine.
 */
class FlexibleEngineTest {

    private static Provider flexibleEngineProvider;

    @Test
    void setUp() {
        flexibleEngineProvider = new FlexibleEngine();
    }

    @Test
    public void testGetProvider() {
        String region = "us-west-2";

        String providerInfo = flexibleEngineProvider.getProvider(region);
        String expectedProviderInfo = """         
                terraform {
                  required_providers {
                    flexibleengine = {
                      source  = "FlexibleEngineCloud/flexibleengine"
                      version = ">= 1.30.0"
                    }
                  }
                }
                                                    
                provider "flexibleengine" {
                  region = "us-west-2"
                }
                """;

        assertEquals(expectedProviderInfo, providerInfo);
    }

    @Test
    public void testGetCsp() {
        Csp csp = flexibleEngineProvider.getCsp();

        assertEquals(Csp.FLEXIBLE_ENGINE, csp);
    }
}
