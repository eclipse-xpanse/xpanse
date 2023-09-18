/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.servicetemplate.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.springframework.hateoas.RepresentationModel;

/**
 * Defines view object for query registered service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceTemplateVo extends RepresentationModel<ServiceTemplateVo> {

    @NotNull
    @Schema(description = "ID of the registered service.")
    private UUID id;

    @NotNull
    @NotBlank
    @Schema(description = "Name of the registered service.")
    private String name;

    @NotNull
    @NotBlank
    @Schema(description = "Version of the registered service.")
    private String version;

    @NotNull
    @Schema(description = "Csp of the registered service.")
    private Csp csp;

    @NotNull
    @Schema(description = "Category of the registered service.")
    private Category category;

    @NotNull
    @NotBlank
    @Schema(description = "Namespace of the user who registered service template.")
    private String namespace;

    @NotNull
    @Schema(description = "Ocl model of the registered service.")
    private Ocl ocl;

    @NotNull
    @Schema(description = "createTime of the registered service.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @Schema(description = "Last updateTime of the registered service.")
    private OffsetDateTime lastModifiedTime;

    @NotNull
    @Schema(description = "State of service.")
    private ServiceRegistrationState serviceRegistrationState;

}
