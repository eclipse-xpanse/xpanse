/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;

/** Define view object for details of the service order. */
@Data
public class ServiceOrderDetails {

    @NotNull
    @Schema(description = "The id of the service order.")
    private UUID orderId;

    @NotNull
    @Schema(description = "The id of the deployed service.")
    private UUID serviceId;

    @NotNull
    @Schema(description = "The task type of the service order.")
    private ServiceOrderType taskType;

    @NotNull
    @Schema(description = "The task status of the service order.")
    private TaskStatus taskStatus;

    @Schema(description = "The id of the original service.")
    private UUID originalServiceId;

    @Schema(description = "The id of the parent service order.")
    private UUID parentOrderId;

    @Schema(description = "The id of the workflow.")
    private String workflowId;

    @Schema(description = "The error response if the service order task failed.")
    private ErrorResponse errorResponse;

    @Schema(description = "The id of the user who created the service order.")
    private String userId;

    @NotNull
    @Schema(description = "The started time of the service order.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime startedTime;

    @Schema(description = "The completed time of the service order.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime completedTime;

    @Schema(description = "The requests of the service order.")
    private Map<String, Object> requestBody;

    @Schema(description = "The result properties of the service order.")
    private Map<String, Object> resultProperties;
}
