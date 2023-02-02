/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions;

/**
 * Defines possible exceptions returned by Terraform execution.
 */
public class TerraformExecutorException extends RuntimeException {

    public TerraformExecutorException() {
        super("TFExecutor Exception");
    }

    public TerraformExecutorException(String message) {
        super("TFExecutor Exception:" + message);
    }

    public TerraformExecutorException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Exception thrown.
     *
     * @param cmd    command that was executed in Terraform.
     * @param output Output of the command execution.
     * @param ex     Type of the exception thrown.
     */
    public TerraformExecutorException(String cmd, String output, Throwable ex) {
        super("TfExecutor Exception:\n"
                        + "\n** Cmd:\n" + cmd + "\n** Output:\n" + output,
                ex);
    }

    public TerraformExecutorException(String cmd, String output) {
        super("TfExecutor Exception:\n"
                + "\n** Cmd:\n" + cmd + "\n** Output:\n" + output);
    }
}
