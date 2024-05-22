/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.logging;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HttpRequest;

/**
 * Custom unique ID generated per request by Logbook.
 */
public class CustomRequestIdGenerator implements CorrelationId {

    public static final String TASK_ID = "TASK_ID";

    @Override
    public String generate(@NonNull HttpRequest request) {
        String uuid = UUID.randomUUID().toString();
        MDC.put(TASK_ID, uuid);
        return uuid;
    }
}
