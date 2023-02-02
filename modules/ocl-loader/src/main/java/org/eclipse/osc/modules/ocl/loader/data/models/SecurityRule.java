/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Defines security rules to be applied for the managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityRule extends RuntimeBase {

    private String name;
    private Integer priority;
    private String protocol;
    private String cidr;
    private String direction;
    private String ports;
    private String action;

}
