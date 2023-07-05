/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformProviderNotFoundException;
import org.junit.jupiter.api.Test;

/**
 * Test of TerraformProviders.
 */
class TerraformProvidersTest {

    @Test
    void testGetProvider() {
        assertThrows(TerraformProviderNotFoundException.class,
                () -> TerraformProviders.getProvider(Csp.ALICLOUD));
    }

}
