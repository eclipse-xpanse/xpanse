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
 * Test of Openstack.
 */
class OpenstackTest {

    private static Provider openStackProvider;

    @Test
    void setUp() {
        openStackProvider = new Openstack();
    }

    @Test
    public void testGetProvider() {
        String region = "us-west-2";

        String providerInfo = openStackProvider.getProvider(">= 1.48.0", region);
        String expectedProviderInfo = """
                terraform {
                  required_providers {
                    openstack = {
                          source  = "terraform-provider-openstack/openstack"
                          version = ">= 1.48.0"
                        }
                  }
                }
                            
                provider "openstack" {
                  region = "us-west-2"
                }
                """;

        assertEquals(expectedProviderInfo, providerInfo);
    }

    @Test
    public void testGetCsp() {
        Csp csp = openStackProvider.getCsp();

        assertEquals(Csp.OPENSTACK, csp);
    }
}
