/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.StorageSizeUnit;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.StorageType;

/**
 * Defines the storage requirements for a managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends RuntimeBase {

    private String name;
    private StorageType type;
    private Integer size;
    private StorageSizeUnit sizeUnit;

}
