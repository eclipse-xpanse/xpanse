/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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

    @Length(min = 2, max = 64)
    @Schema(description = "The name of the security group")
    private String name;

    @Valid
    @Schema(description = "The list of the security rules")
    private List<SecurityRule> rules;

}
