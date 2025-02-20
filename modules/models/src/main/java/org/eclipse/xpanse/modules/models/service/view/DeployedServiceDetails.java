/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.view;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;

/** Define view object for detail of the deployed service. */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeployedServiceDetails extends DeployedService {

    @Schema(description = "The resource list of the deployed service.")
    private List<@Valid DeployResource> deployResources;

    @Schema(description = "The properties of the deployed service.")
    private Map<String, String> deployedServiceProperties;
}
