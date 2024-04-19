/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.orchestrator.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Data;
import lombok.ToString;
import org.eclipse.xpanse.modules.models.common.enums.Csp;

/**
 * Audit log object.
 */
@Data
@ToString(callSuper = true)
public class AuditLog {

    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the method.")
    private String methodName;

    @NotBlank
    @NotEmpty
    @Schema(description = "The type of the method.")
    private String methodType;

    @NotBlank
    @NotEmpty
    @Schema(description = "The address of the method request.")
    private String url;

    @NotNull
    @Schema(description = "The params of the method request.")
    private Object[] params;

    @NotNull
    @Schema(description = "The result of method request.")
    private Object result;

    @NotNull
    @Schema(description = "cloud service provider.")
    private Csp csp;

    @Schema(description = "The ID of Operator.")
    private String userId;

    @NotNull
    @Schema(description = "The time of Operating.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime operatingTime;

}