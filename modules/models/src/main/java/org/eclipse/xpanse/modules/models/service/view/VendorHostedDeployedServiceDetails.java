/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.view;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;

/** Define view object for vendor hosted detail of the deployed service. */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorHostedDeployedServiceDetails extends DeployedService {

    @NotNull
    @Schema(description = "The create request of the deployed service.")
    private DeployRequest deployRequest;

    @Schema(description = "The properties of the deployed service.")
    private Map<String, String> deployedServiceProperties;
}
