package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import lombok.extern.java.Log;
import org.eclipse.osc.services.ocl.loader.Ocl;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
public abstract class AtomBuilder {

    public enum State {
        INITIAL,
        PENDING,
        RUNNING,
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

    private final List<AtomBuilder> children = new ArrayList<AtomBuilder>();

    private Ocl mOcl;

    public AtomBuilder(Ocl ocl) {
        mOcl = ocl;
        setTimeOut(300);
    }

    public void addSubBuilder(AtomBuilder builder) {
        builder.father = this;
        children.add(builder);
    }

    public boolean build(BuilderContext ctx) {
        for (AtomBuilder subBuilder: children) {
            subBuilder.build(ctx);
        }

        setState(State.RUNNING);
        long startTime = System.currentTimeMillis();

        if (!children.isEmpty()) {
            AtomBuilder longest = children.stream().max(Comparator.comparing(AtomBuilder::getTimeOut)).get();

            while (true) {
                if (children.stream().allMatch(builder -> (builder.getState() == State.SUCCESS))) {
                    break;
                }

                if (children.stream().anyMatch(builder -> (builder.getState() == State.FAILED))) {
                    setState(State.FAILED);
                    // Todo: give out the specified failed reason with setLastFail().
                    return false;
                }

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage());
                }

                if ((System.currentTimeMillis() - startTime) > longest.getTimeOut() * 1000) {
                    setState(State.FAILED);
                    setLastFail("Builder Timeout " + name());
                    break;
                }
            }
        }

        if (!create(ctx)) {
            return false;
        };

        setState(State.SUCCESS);
        return true;
    }

    public boolean rollback(BuilderContext ctx) {
        for (AtomBuilder subBuilder: children) {
            subBuilder.rollback(ctx);
        }

        setState(State.RUNNING);
        long startTime = System.currentTimeMillis();

        if (!children.isEmpty()) {
            AtomBuilder longest = children.stream().max(Comparator.comparing(AtomBuilder::getTimeOut)).get();

            while (true) {
                if (children.stream().allMatch(builder -> (builder.getState() == State.INITIAL))) {
                    break;
                }

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage());
                }

                if ((System.currentTimeMillis() - startTime) > longest.getTimeOut() * 1000) {
                    setState(State.FAILED);
                    setLastFail("Builder Timeout " + name());
                    break;
                }
            }
        }
        if (!destroy(ctx)) {
            return false;
        };

        setState(State.INITIAL);
        return true;
    }

    public abstract boolean create(BuilderContext ctx);

    public abstract boolean destroy(BuilderContext ctx);

}
