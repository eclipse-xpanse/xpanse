/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Data model that describes in which mode the controller application of the service will be
 * deployed. Different CSPs have different service management deployments.
 */
@Data
public class ServiceControllerMode {

    @Schema(
            description =
                    "describes if the controller handles region specific deployment or is it a"
                            + " common deployment across regions. ")
    private Boolean isRegionSpecificController;

    @Schema(
            description =
                    "service supports multiple versions. If not, the version is fixed for the"
                            + " specific controller.")
    private Boolean isSupportsMultipleVersions;

    @Schema(description = "Does the controller deploy on different CSPs or is it CSP specific.")
    private Boolean isSupportsMultipleCloudProviders;

    @Schema(
            description =
                    "Describes if the controller allows different hosting types for this service.")
    private Boolean isSupportsMultipleHostingTypes;
}
