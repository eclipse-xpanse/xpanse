/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** Defines the identifier of the service object. */
@Valid
@Data
@Slf4j
public class ObjectIdentifier implements Serializable {

    @Serial private static final long serialVersionUID = 2723212494104579585L;

    @NotNull
    @Schema(description = "the name of service object identifier.")
    private String name;

    @NotNull
    @Schema(description = "the value schema of service object identifier.")
    private String valueSchema;
}
