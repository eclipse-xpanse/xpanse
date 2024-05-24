/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing.exceptions;

/**
 * Exception thrown when calculating the service price failed.
 */
public class ServicePriceCalculationFailed extends RuntimeException {

    public ServicePriceCalculationFailed(String message) {
        super(message);
    }
}

