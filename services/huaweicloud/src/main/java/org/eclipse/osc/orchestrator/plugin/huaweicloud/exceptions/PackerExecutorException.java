package org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions;

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
