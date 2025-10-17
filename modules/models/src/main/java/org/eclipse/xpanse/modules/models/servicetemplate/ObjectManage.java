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
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ControllerApiMethods;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ObjectActionType;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines the service object manage of the service. */
@Valid
@Data
@Slf4j
public class ObjectManage {

    @NotNull
    @Schema(description = "the type of service object action.")
    private ObjectActionType objectActionType;

    @NotNull
    @Schema(description = "the modificationImpact of service object manage.")
    private ModificationImpact modificationImpact;

    @NotNull
    @Schema(description = "the handler script of service object manage.")
    private ServiceChangeScript objectHandlerScript;

    @Size(min = 1)
    @UniqueElements
    @Schema(
            description =
                    "The service object parameters of service .The list elements must be unique."
                            + " All parameters are put together to build a JSON 'object' with each"
                            + " parameter as a property of this object.")
    private List<ObjectParameter> objectParameters;

    @Schema(
            description =
                    "controller methods for service objects. There can be one or more methods to"
                            + " manage objects and one method to read objects.")
    private ControllerApiMethods controllerApiMethods;
}
