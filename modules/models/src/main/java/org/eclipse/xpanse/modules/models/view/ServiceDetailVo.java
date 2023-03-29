/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.view;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.service.DeployResource;

/**
 * Define view object for detail of the deployed service.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceDetailVo extends ServiceVo {

    @NotNull
    @Schema(description = "The create request of the deployed service.")
    private CreateRequest createRequest;

    @Schema(description = "The resource list of the deployed service.")
    private List<DeployResource> deployResources;

    @Schema(description = "The output property of the deployed service.")
    private Map<String, String> property;
}
