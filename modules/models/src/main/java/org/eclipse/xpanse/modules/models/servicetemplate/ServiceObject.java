/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ConfigurationManagerTool;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines the service object of the service. */
@Valid
@Data
@Slf4j
public class ServiceObject implements Serializable {

    @Serial private static final long serialVersionUID = 240913796673021261L;

    @NotNull
    @Schema(description = "the name of service object.")
    private String name;

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
