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

/** Defines the handler script of service object manage. */
@Valid
@Data
@Slf4j
public class ObjectHandlerScript implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(
            description =
                    "Means should the service object update run on each node "
                            + "of the specific component or just one.")
    private Boolean runOnlyOnce;

    @NotNull
    @Schema(description = "Ansible script service object handler.")
    private AnsibleScriptConfig ansibleScriptConfig;
}
