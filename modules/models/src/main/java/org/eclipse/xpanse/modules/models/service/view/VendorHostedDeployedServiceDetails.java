/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.view;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Define view object for vendor hosted detail of the deployed service. */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorHostedDeployedServiceDetails extends DeployedService {

    @Schema(description = "The properties of the deployed service.")
    private Map<String, String> deployedServiceProperties;
}
