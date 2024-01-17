/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * The contact details of the Cloud Service Provider.
 */
@Data
public class ServiceProviderContactDetails {

    @Valid
    @Size(min = 1)
    @Schema(description = "The email details of the service provider.")
    private List<String> email;

    @Valid
    @Size(min = 1)
    @Schema(description = "The phone details of the service provider.")
    private List<String> phone;

    @Valid
    @Size(min = 1)
    @Schema(description = "The chat details of the service provider.")
    private List<String> chat;

    @Valid
    @Size(min = 1)
    @Schema(description = "The website details of the service provider.")
    private List<String> website;
}
