/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceobject;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

/** Service Object Request Model. */
@Data
public class ServiceObjectRequest {

    @NotNull
    @Size(min = 1)
    @Schema(description = "The name of service object identifier.")
    private String objectIdentifier;

    @Schema(description = "The collection of dependent object IDs.")
    private List<UUID> linkedObjects;

    @NotNull
    @Size(min = 1)
    @Schema(description = "The collection of service object parameter.")
    private Map<String, Object> serviceObjectParameters;

    @Hidden private UUID objectId;
    @Hidden private String objectType;
}
