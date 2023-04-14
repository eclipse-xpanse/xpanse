/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.utils;

import lombok.Data;

/**
 * Encapsulates result of system command execution.
 */
@Data
public class SystemCmdResult {

    private String commandExecuted;
    private boolean isCommandSuccessful;
    private String commandOutput;

}
