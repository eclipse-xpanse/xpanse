/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Defines Virtual Private Cloud configuration for the managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Vpc extends RuntimeBase {

    private String name;
    private String cidr;

}
