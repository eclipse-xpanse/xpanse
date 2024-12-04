/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.servicetemplate.change;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateChangeStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateRequestType;

/**
 * Defines view object for service template history.
 */
@Data
public class ServiceTemplateHistoryVo {

    @NotNull
    @Schema(description = "ID of the registered service template.")
    private UUID serviceTemplateId;

    @NotNull
    @Schema(description = "ID of the change history of the service template.")
    private UUID changeId;

    @NotNull
    @Schema(description = "Type of the request.")
    private ServiceTemplateRequestType requestType;

    @NotNull
    @Schema(description = "Status of the request.")
    private ServiceTemplateChangeStatus status;

    @Schema(description = "Comment of the review request.")
    private String reviewComment;

    @Schema(description = "Status of the request.")
    private Boolean blockTemplateUntilReviewed;

    @NotNull
    @Schema(description = "Create time of the service template request.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @Schema(description = "Last update time of the service template request.")
    private OffsetDateTime lastModifiedTime;
}
