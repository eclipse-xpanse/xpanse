/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Subnet information on which the managed service will be deployed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Subnet extends RuntimeBase {

    private String vpc;
    private String name;
    private String cidr;

}
