/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.UserDataType;

/**
 * user data for Vm.
 */
@Data
public class UserData {

    @NotNull
    @Schema(description = "The type of the UserData, valid values: shell, powershell")
    private UserDataType type;

    @NotNull
    @NotEmpty
    @Valid
    @Schema(description = "The commands of the UserData")
    private List<String> commands;

}
