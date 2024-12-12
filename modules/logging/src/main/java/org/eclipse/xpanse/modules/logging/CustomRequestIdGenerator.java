/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.logging;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.ORDER_ID;
import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.TRACKING_ID;

import java.util.Objects;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HttpRequest;

/** Custom unique ID generated per request by Logbook. */
public class CustomRequestIdGenerator implements CorrelationId {

    /**
     * Generate order id for order request.
     *
     * @return order id.
     */
    public static UUID generateOrderId() {
        UUID orderId = UUID.randomUUID();
        MDC.put(ORDER_ID, orderId.toString());
        return orderId;
    }

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
