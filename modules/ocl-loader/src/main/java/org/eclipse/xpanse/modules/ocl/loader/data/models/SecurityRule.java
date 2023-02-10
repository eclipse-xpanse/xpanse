/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.SecurityRuleAction;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.SecurityRuleDirection;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.SecurityRuleProtocol;

/**
 * Defines security rules to be applied for the managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityRule extends RuntimeBase {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the security rule")
    private String name;

    @NotNull
    @Schema(description = "The priority of the security rule")
    private Integer priority;

    @NotNull
    @Schema(description = "The protocol of the security rule, valid values: tcp,udp")
    private SecurityRuleProtocol protocol;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The cidr for the security rule, for example: 192.168.9.0/24")
    private String cidr;

    @NotNull
    @Schema(description = "The direction for the security rule, valid values: in,out")
    private SecurityRuleDirection direction;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description =
            "The ports for the security rule, for example: 80/1024-65536/80,8000,8080")
    private String ports;

    @NotNull
    @Schema(description = "The action for the security rule, valid values: allow,deny")
    private SecurityRuleAction action;

}
