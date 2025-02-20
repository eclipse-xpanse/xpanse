/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.AnsibleScriptConfig;

/** Request object to describe the service change to be executed. */
@Data
public class ServiceChangeRequest {

    @NotNull
    @Schema(description = "Id of the change request")
    private UUID changeId;

    @NotNull
    @Schema(
            description =
                    "request parameters to be used to execute the change scripts. In case of"
                            + " Ansible, this will be used as extra vars.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> serviceChangeParameters;

    @Schema(description = "defines the ansible script information.")
    private AnsibleScriptConfig ansibleScriptConfig;

    @Schema(
            description = "Inventory information for Ansible script.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> ansibleInventory;
}
