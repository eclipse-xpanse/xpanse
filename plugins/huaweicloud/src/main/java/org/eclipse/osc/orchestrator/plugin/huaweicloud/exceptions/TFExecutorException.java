/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions;

public class TFExecutorException extends RuntimeException {

    public TFExecutorException() {
        super("TFExecutor Exception");
    }

    public TFExecutorException(String message) {
        super("TFExecutor Exception:" + message);
    }

    public TFExecutorException(String message, Throwable ex) {
        super(message, ex);
    }

    public TFExecutorException(String cmd, String output, Throwable ex) {
        super("TFExecutor Exception:\n"
                + "\n** Cmd:\n" + cmd + "\n** Output:\n" + output,
            ex);
    }

    public TFExecutorException(String cmd, String output) {
        super("TFExecutor Exception:\n"
            + "\n** Cmd:\n" + cmd + "\n** Output:\n" + output);
    }
}
