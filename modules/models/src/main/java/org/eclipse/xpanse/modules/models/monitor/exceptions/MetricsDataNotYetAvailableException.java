/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.monitor.exceptions;

/** Throw exception when monitor or metrics data is not yet ready or unavailable. */
public class MetricsDataNotYetAvailableException extends RuntimeException {

    public MetricsDataNotYetAvailableException(String message) {
        super(message);
    }
}
