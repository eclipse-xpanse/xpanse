/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow.migrate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequestBase;

/**
 * MigrateRequest model.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class MigrateRequest extends DeployRequestBase {

    @Serial
    private static final long serialVersionUID = 204243455244611956L;

    /**
     * The id of the service to migrate.
     */
    @NotNull
    @Schema(description = "The id of the service to migrate")
    private UUID id;
}
