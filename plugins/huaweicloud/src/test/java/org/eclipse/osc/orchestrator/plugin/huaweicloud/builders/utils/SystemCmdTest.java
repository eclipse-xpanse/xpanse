/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemCmdTest {

    @Test
    public void executeTest() {
        SystemCmd systemCmd = new SystemCmd();
        String os = System.getProperty("os.name").toLowerCase();
        StringBuilder strOut = new StringBuilder();
        if (os.contains("linux")) {
            Assertions.assertTrue(systemCmd.execute("date", strOut));
        } else if (os.contains("windows")) {
            Assertions.assertTrue(systemCmd.execute("where cmd", strOut));
        }
    }
}
