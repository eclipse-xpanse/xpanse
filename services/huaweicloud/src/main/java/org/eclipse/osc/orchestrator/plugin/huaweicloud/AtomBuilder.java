package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import lombok.extern.java.Log;
import org.eclipse.osc.services.ocl.loader.Ocl;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log
public abstract class AtomBuilder {

    public enum State {
        PENDING,
        RUNNING,
        DELETING,
        SUCCESS,
        FAILED
    }

    public String name() {
        return "AtomBuilder";
    }

    @Setter@Getter
    private State state;

    @Setter@Getter
    private long timeOut;

    @Setter@Getter
    private String lastFail;

    @Getter
    private AtomBuilder father;

    private final List<AtomBuilder> children;

    private Ocl mOcl;

    public AtomBuilder(Ocl ocl) {
        mOcl = ocl;
        children = new ArrayList<>();
        setTimeOut(300);
    }

    public void addSubBuilder(AtomBuilder builder) {
        builder.father = this;
        children.add(builder);
    }

    public boolean needWaiting() {
        return state == State.DELETING || state == State.RUNNING;
    }

    public boolean build(BuilderContext ctx) {
        setState(State.RUNNING);

        for (AtomBuilder subBuilder: children) {
            if (!subBuilder.build(ctx)) {
                setState(State.FAILED);
                log.warning("Submit build failed." + subBuilder.name());
                return false;
            }
        }

        long startTime = System.currentTimeMillis();
        long timeOut = 100 * 1000;
        if (!children.isEmpty()) {
            AtomBuilder longest = children.stream().max(Comparator.comparing(AtomBuilder::getTimeOut)).get();
            timeOut = longest.getTimeOut() * 1000;
        }

        while (!children.isEmpty() && children.stream().allMatch(AtomBuilder::needWaiting)) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ex) {
                log.info(ex.getMessage());
            }

            if ((System.currentTimeMillis() - startTime) > timeOut) {
                setState(State.FAILED);
                setLastFail("Builder Timeout " + name());
                return false;
            }
        }

        if (children.stream().anyMatch(builder -> (builder.getState() == State.FAILED))) {
            setState(State.FAILED);
            // Todo: give out the specified failed reason with setLastFail().
            return false;
        }

        if (!create(ctx)) {
            setState(State.FAILED);
            return false;
        };

        setState(State.SUCCESS);
        return true;
    }

    public boolean rollback(BuilderContext ctx) {
        setState(State.DELETING);

        if (!destroy(ctx)) {
            log.warning("Builder destroy failed." + name());
            setLastFail("Builder destroy failed." + name());
            setState(State.FAILED);
        } else {
            setState(State.PENDING);
        };

        for (AtomBuilder subBuilder: children) {
            if (!subBuilder.rollback(ctx)) {
                setState(State.FAILED);
            }
        }

        long startTime = System.currentTimeMillis();
        long timeOut = 100 * 1000;
        if (!children.isEmpty()) {
            AtomBuilder longest = children.stream().max(Comparator.comparing(AtomBuilder::getTimeOut)).get();
            timeOut = longest.getTimeOut() * 1000;
        }

        while (!children.isEmpty() && children.stream().allMatch(AtomBuilder::needWaiting)
                && (System.currentTimeMillis() - startTime) <= timeOut) {

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ex) {
                log.info(ex.getMessage());
            }
        }

        return getState() == State.PENDING;
    }

    public abstract boolean create(BuilderContext ctx);

    public abstract boolean destroy(BuilderContext ctx);

}
