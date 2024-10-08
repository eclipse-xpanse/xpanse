/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow.recreate.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;

/**
 * Define view object for detail of the service recreate.
 */
@Data
public class ServiceRecreateDetails {

    @NotNull
    @Schema(description = "The ID of the service recreate")
    private UUID recreateId;

    @NotNull
    @Schema(description = "The ID of the old service")
    private UUID serviceId;

    @NotNull
    @Schema(description = "The status of the service recreate")
    private RecreateStatus recreateStatus;

    @NotNull
    @Schema(description = "Time of service recreate.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createTime;

    @NotNull
    @Schema(description = "Time of update service recreate.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime lastModifiedTime;

}
