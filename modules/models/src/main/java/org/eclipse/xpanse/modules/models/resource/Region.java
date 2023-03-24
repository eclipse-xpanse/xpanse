/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import lombok.Data;

/**
 * The regions of the Cloud Service Provider.
 */
@Data
public class Region {

    private String name;
    private String area;

}
