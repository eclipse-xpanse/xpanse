/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.logging;

import java.util.Objects;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HttpRequest;

/**
 * Custom unique ID generated per request by Logbook.
 */
public class CustomRequestIdGenerator implements CorrelationId {

    /**
     * The key of the task id in MDC.
     */
    public static final String TASK_ID = "TASK_ID";

    /**
     * The key of the tracking id in MDC.
     */
    public static final String TRACKING_ID = "TRACKING_ID";

    @Override
    public String generate(@NonNull HttpRequest request) {
        if (Objects.nonNull(MDC.get(TRACKING_ID))) {
            return MDC.get(TRACKING_ID);
        } else {
            String uuid = UUID.randomUUID().toString();
            MDC.put(TRACKING_ID, uuid);
            return uuid;
        }
    }
}
