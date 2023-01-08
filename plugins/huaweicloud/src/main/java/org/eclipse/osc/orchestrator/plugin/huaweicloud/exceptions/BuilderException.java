package org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions;

import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;

public class BuilderException extends RuntimeException {

    public BuilderException(AtomBuilder builder) {
        super("Builder Exception: [" + builder.name() + "] ");
    }

    public BuilderException(AtomBuilder builder, String message) {
        super("Builder Exception: [" + builder.name() + "] " + message);
    }

    public BuilderException(AtomBuilder builder, String message, Throwable ex) {
        super("Builder Exception: [" + builder.name() + "] " + message, ex);
    }
}
