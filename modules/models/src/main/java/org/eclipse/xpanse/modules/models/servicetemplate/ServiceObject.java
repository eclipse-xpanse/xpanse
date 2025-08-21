/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ConfigurationManagerTool;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines the service object of the service. */
@Valid
@Data
@Slf4j
public class ServiceObject {

    @NotNull
    @Schema(description = "the type of service object.")
    private String type;

    @NotNull
    @Schema(description = "the identifier of service object.")
    private ObjectIdentifier objectIdentifier;

    @NotNull
    @Schema(description = "the tool used to manage the service object.")
    private ConfigurationManagerTool handlerType;

    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The collection of the service object manage.")
    private List<ObjectManage> objectsManage;
}
