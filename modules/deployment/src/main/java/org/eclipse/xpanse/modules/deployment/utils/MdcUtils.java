/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.utils;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.ORDER_ID;
import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.SERVICE_ID;

import org.slf4j.MDC;

/** The tools of MDC. */
public final class MdcUtils {

    /** Put serviceId and orderId to MDC. */
    public static void putServiceIdAndOrderId(String serviceId, String orderId) {
        MDC.put(SERVICE_ID, serviceId);
        MDC.put(ORDER_ID, orderId);
    }
}
