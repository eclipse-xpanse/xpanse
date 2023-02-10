/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * Base class which holds the security rule information.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityGroup extends RuntimeBase {

    @NotNull
    @NotBlank
    @NotEmpty
    @Length(min = 2, max = 64)
    @Schema(description = "The name of the security group")
    private String name;

    @Valid
    @NotNull
    @NotEmpty
    @Schema(description = "The list of the security rules")
    private List<SecurityRule> rules;

}
