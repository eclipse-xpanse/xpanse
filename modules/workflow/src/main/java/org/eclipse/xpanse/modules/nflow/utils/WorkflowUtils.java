package org.eclipse.xpanse.modules.nflow.utils;

import io.nflow.engine.workflow.definition.WorkflowSettings;
import org.joda.time.Duration;
import org.joda.time.Period;

public class WorkflowUtils {

    public static WorkflowSettings getWorkflowSettings() {

        return new WorkflowSettings.Builder()
                .setMinErrorTransitionDelay(Duration.millis(3000))
                .setMaxErrorTransitionDelay(Duration.millis(1000))
                .setShortTransitionDelay(Duration.millis(2000))
                .setMaxRetries(10)
                .setMaxSubsequentStateExecutions(10)
                .setHistoryDeletableAfter(Period.days(30))
                .setDeleteHistoryCondition(WorkflowSettings.Builder.oncePerDay())
                .setDefaultPriority((short) 10)
                .build();
    }
}
