package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.enums.BuilderState;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public abstract class AtomBuilder {

    public String name() {
        return "AtomBuilder";
    }

    @Setter
    @Getter
    private BuilderState state;

    @Setter
    @Getter
    private long timeout = TimeUnit.MICROSECONDS.toSeconds(300);

    @Setter
    @Getter
    private String lastFail;

    @Getter
    private AtomBuilder parent;

    private final List<AtomBuilder> subBuilders = new ArrayList<>();

    protected final Ocl ocl;

    public AtomBuilder(Ocl ocl) {
        this.ocl = ocl;
    }

    /**
     * Add a sub-builder for this. The current builder will not start to build until all the sub
     * builders were successfully built.
     */
    public void addSubBuilder(AtomBuilder builder) {
        builder.parent = this;
        subBuilders.add(builder);
    }

    /**
     * If return true, that means the builder is working in progress.
     *
     * @return Ether or not need to be waiting.
     */
    private boolean needWaiting() {
        return state == BuilderState.DELETING || state == BuilderState.RUNNING;
    }

    private boolean waitSubBuilders() {
        long startTime = System.currentTimeMillis();
        long timeToWait = TimeUnit.MICROSECONDS.toSeconds(100);
        if (!subBuilders.isEmpty()) {
            timeToWait = subBuilders.stream()
                .max(Comparator.comparing(AtomBuilder::getTimeout))
                .get()
                .getTimeout();
        }

        while (!subBuilders.isEmpty() && subBuilders.stream().allMatch(AtomBuilder::needWaiting)) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ex) {
                log.warn("Timeout", ex);
                Thread.currentThread().interrupt();
            }

            if ((System.currentTimeMillis() - startTime) > timeToWait) {
                setState(BuilderState.FAILED);
                setLastFail("Builder Timeout " + name());
                return false;
            }
        }
        return true;
    }

    /**
     * Builders will be started in sequence. Util all the sub builders going to be successful. Then
     * the current builder will start to build.
     *
     * @param ctx The building context for the whole building progress.
     * @return The building result, true or false. More detailed state will be present by @state.
     */
    public boolean build(BuilderContext ctx) {
        setState(BuilderState.RUNNING);

        for (AtomBuilder subBuilder : subBuilders) {
            if (!subBuilder.build(ctx)) {
                setState(BuilderState.FAILED);
                log.error("Submit builder: {} failed.", subBuilder.name());
                return false;
            }
        }

        if (!waitSubBuilders()) {
            log.error("Wait sub builders failed.");
            return false;
        }

        if (subBuilders.stream().anyMatch(builder -> builder.getState() == BuilderState.FAILED)) {
            setState(BuilderState.FAILED);
            // Todo: give out the specified failed reason with setLastFail().
            return false;
        }

        if (!create(ctx)) {
            setState(BuilderState.FAILED);
            return false;
        }

        setState(BuilderState.SUCCESS);
        return true;
    }

    /**
     * Builders will be started to roll back in sequence. The current builder will begin to roll
     * back first. Due to the try-my-best strategy, the @rollback() result may be ignored.
     *
     * @param ctx The building context for the whole building progress.
     * @return The rollback result, true or false. More detailed state will be present by @state.
     */
    public boolean rollback(BuilderContext ctx) {
        setState(BuilderState.DELETING);

        if (!destroy(ctx)) {
            log.error("Builder: {} destroy failed.", name());
            setLastFail("Builder destroy failed." + name());
            setState(BuilderState.FAILED);
        } else {
            setState(BuilderState.PENDING);
        }

        for (AtomBuilder subBuilder : subBuilders) {
            if (!subBuilder.rollback(ctx)) {
                setState(BuilderState.FAILED);
            }
        }

        if (!waitSubBuilders()) {
            log.error("Wait sub builders failed.");
        }

        return getState() == BuilderState.PENDING;
    }

    /**
     * Creating actions for current builder.
     *
     * @param ctx The building context for the whole building progress.
     * @return The creating result, true or false.
     */
    public abstract boolean create(BuilderContext ctx);

    /**
     * Destroying actions for current builder.
     *
     * @param ctx The building context for the whole building progress.
     * @return The creating result, true or false.
     */
    public abstract boolean destroy(BuilderContext ctx);
}
