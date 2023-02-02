/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Defines the storage requirements for a managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends RuntimeBase {

    private String name;
    private String type;
    private String size;

}
