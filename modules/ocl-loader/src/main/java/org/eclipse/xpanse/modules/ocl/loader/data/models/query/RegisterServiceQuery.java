/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.modules.ocl.loader.data.models.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

/**
 * The query model for search register services.
 */
@Data
@Valid
public class RegisterServiceQuery {

    @Schema(description = "Name of the cloud service provider.")
    private String cspName;

    @Schema(description = "Name of the registered service.")
    private String serviceName;

    @Schema(description = "Version of the registered service.")
    private String serviceVersion;
}
