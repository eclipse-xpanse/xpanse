/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.logging;

/**
 * Constant keys for logging.
 */
public class LoggingKeyConstant {

    /**
     * The key of the tracking id associated with the request in MDC.
     */
    public static final String TRACKING_ID = "TRACKING_ID";

    /**
     * The key of the service id associated with the request in MDC.
     */
    public static final String SERVICE_ID = "SERVICE_ID";

    /**
     * The key of the order id associated with the request in MDC.
     */
    public static final String ORDER_ID = "ORDER_ID";

    /**
     * The key of the X-Tracking-ID associated with the response in Header.
     */
    public static final String HEADER_TRACKING_ID = "X-Tracking-ID";

    private LoggingKeyConstant() {
    }
}
