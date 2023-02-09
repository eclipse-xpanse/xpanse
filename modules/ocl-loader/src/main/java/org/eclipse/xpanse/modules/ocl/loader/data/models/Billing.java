/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import lombok.Data;

/**
 * Defines the billing model of the managed service.
 */
@Data
public class Billing {

    private String model;
    private String period;
    private String currency;
    private Double fixedPrice;
    private Double variablePrice;

}
