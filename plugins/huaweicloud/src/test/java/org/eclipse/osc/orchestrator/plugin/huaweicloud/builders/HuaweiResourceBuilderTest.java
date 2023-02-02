/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiResourceBuilderTest {

    @Test
    public void builderTest() {
        HuaweiResourceBuilder builder = new HuaweiResourceBuilder(null);
        Assertions.assertThrows(BuilderException.class, () -> builder.create(null));
    }
}
