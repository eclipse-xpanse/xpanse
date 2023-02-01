/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiEnvBuilderTest {

    @Test
    public void builderTest() {
        HuaweiEnvBuilder builder = new HuaweiEnvBuilder(null);
        Assertions.assertThrows(BuilderException.class, () -> builder.create(null));
    }
}
