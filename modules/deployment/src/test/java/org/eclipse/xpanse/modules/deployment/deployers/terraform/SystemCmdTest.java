/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.SystemCmd;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.SystemCmdResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemCmdTest {

    @Test
    void systemCommandExecute() {
        SystemCmd systemCmd = new SystemCmd();
        SystemCmdResult systemCmdResult;
        if (SystemUtils.IS_OS_WINDOWS) {
            systemCmdResult = systemCmd.execute("cmd.exe /c echo hello");
        } else {
            systemCmdResult = systemCmd.execute("echo hello");
        }

        Assertions.assertTrue(systemCmdResult.isCommandSuccessful());
        Assertions.assertEquals("hello", systemCmdResult.getCommandStdOutput());
        Assertions.assertEquals("", systemCmdResult.getCommandStdError());
    }
}
