/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.modules.models.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.resource.Ocl;

/**
 * Define view object for UI Client query registered service.
 */
@Data
public class RegisteredServiceVo {

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
    @Schema(description = "ID of the registered service.")
    private Category category;
    @NotNull
    @Schema(description = "ID of the registered service.")
    private Ocl ocl;

    @NotNull
    @Schema(description = "Time of register service.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Time of update service.")
    private Date lastModifiedTime;

    @NotNull
    @Schema(description = "State of service.")
    private ServiceState serviceState;


}
