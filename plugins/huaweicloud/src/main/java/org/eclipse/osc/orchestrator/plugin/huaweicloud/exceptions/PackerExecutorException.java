/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions;

/**
 * Defines possible exceptions returned by Packer execution.
 */
public class PackerExecutorException extends RuntimeException {

    public PackerExecutorException() {
        super("PackerExecutor Exception");
    }

    public PackerExecutorException(String message) {
        super("PackerExecutor Exception:" + message);
    }

    public PackerExecutorException(String message, Throwable ex) {
        super(message, ex);
    }

    /**
     * Exception thrown.
     *
     * @param cmd    command that was executed in Terraform.
     * @param output Output of the command execution.
     * @param ex     Type of the exception thrown.
     */
    public PackerExecutorException(String cmd, String output, Throwable ex) {
        super("PackerExecutor Exception:\n"
                        + "\n** Cmd:\n" + cmd + "\n** Output:\n" + output,
                ex);
    }

    public PackerExecutorException(String cmd, String output) {
        super("PackerExecutor Exception:\n"
                + "\n** Cmd:\n" + cmd + "\n** Output:\n" + output);
    }
}
