/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow.migrate.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;

/**
 * Define view object for detail of the service migration.
 */
@Data
public class ServiceMigrationDetails {

    @NotNull
    @Schema(description = "The ID of the service migration")
    private UUID migrationId;

    @NotNull
    @Schema(description = "The ID of the old service")
    private UUID oldServiceId;

    @NotNull
    @Schema(description = "The ID of the new service")
    private UUID newServiceId;

    @NotNull
    @Schema(description = "The status of the service migration")
    private MigrationStatus migrationStatus;

    @NotNull
    @Schema(description = "Time of service migration.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createTime;

    @NotNull
    @Schema(description = "Time of update service migration.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime lastModifiedTime;

}
