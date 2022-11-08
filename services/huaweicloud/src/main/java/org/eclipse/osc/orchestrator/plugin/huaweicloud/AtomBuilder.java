package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.enums.BuilderState;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Log
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

    public void addSubBuilder(AtomBuilder builder) {
        builder.parent = this;
        subBuilders.add(builder);
    }

    public boolean needWaiting() {
        return state == BuilderState.DELETING || state == BuilderState.RUNNING;
    }

    public boolean build(BuilderContext ctx) {
        setState(BuilderState.RUNNING);

        for (AtomBuilder subBuilder : subBuilders) {
            if (!subBuilder.build(ctx)) {
                setState(BuilderState.FAILED);
                log.warning("Submit build failed." + subBuilder.name());
                return false;
            }
        }

        long startTime = System.currentTimeMillis();
        long timeout = TimeUnit.MICROSECONDS.toSeconds(100);
        if (!subBuilders.isEmpty()) {
            timeout = subBuilders.stream()
                          .max(Comparator.comparing(AtomBuilder::getTimeout))
                          .get()
                          .getTimeout();
        }

        while (!subBuilders.isEmpty() && subBuilders.stream().allMatch(AtomBuilder::needWaiting)) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ex) {
                log.info(ex.getMessage());
            }

            if ((System.currentTimeMillis() - startTime) > timeout) {
                setState(BuilderState.FAILED);
                setLastFail("Builder Timeout " + name());
                return false;
            }
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

    public boolean rollback(BuilderContext ctx) {
        setState(BuilderState.DELETING);

        if (!destroy(ctx)) {
            log.warning("Builder destroy failed." + name());
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

        long startTime = System.currentTimeMillis();
        long timeout = TimeUnit.MICROSECONDS.toSeconds(100);
        if (!subBuilders.isEmpty()) {
            timeout = subBuilders.stream()
                          .max(Comparator.comparing(AtomBuilder::getTimeout))
                          .get()
                          .getTimeout();
        }

        while (!subBuilders.isEmpty() && subBuilders.stream().allMatch(AtomBuilder::needWaiting)
            && (System.currentTimeMillis() - startTime) <= timeout) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ex) {
                log.info(ex.getMessage());
            }
        }

        return getState() == BuilderState.PENDING;
    }

    public abstract boolean create(BuilderContext ctx);

    public abstract boolean destroy(BuilderContext ctx);
}
