/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.StorageSizeUnit;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.StorageType;
import org.hibernate.validator.constraints.Length;

/**
 * Defines the storage requirements for a managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends RuntimeBase {

    @NotNull
    @NotBlank
    @NotEmpty
    @Length(min = 3, max = 64)
    @Schema(description = "The name of the storage")
    private String name;

    @NotNull
    @Schema(description = "The type of the storage, valid values: SSD,SAS")
    private StorageType type;

    @Min(value = 1)
    @Max(value = 1024)
    @Schema(description = "The size of the storage, the unit is specified by @sizeUnit")
    private Integer size;

    @NotNull
    @Schema(description = "The sizeUnit of the storage, the size is specified by @size")
    private StorageSizeUnit sizeUnit;

}
