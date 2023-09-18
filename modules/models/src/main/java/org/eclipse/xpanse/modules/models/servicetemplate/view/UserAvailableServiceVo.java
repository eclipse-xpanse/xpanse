/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.servicetemplate.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Billing;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Flavor;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.springframework.hateoas.RepresentationModel;

/**
 * The view model for the end-user to query the available services.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserAvailableServiceVo extends RepresentationModel<UserAvailableServiceVo> {

    @NotNull
    @Schema(description = "The id of the available service.")
    private UUID id;

    @NotNull
    @Schema(description = "The catalog of the available service.")
    private Category category;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the available service.")
    private String name;

    @NotNull
    @NotBlank
    @Schema(description = "The version of the available service.")
    private String version;

    @NotNull
    @Schema(description = "The Cloud Service Provider of the available service.")
    private Csp csp;

    @NotEmpty
    @Schema(description = "The regions of the Cloud Service Provider.")
    private List<@Valid Region> regions;

    @NotNull
    @NotBlank
    @Schema(description = "The description of the available service.")
    private String description;

    @NotNull
    @NotBlank
    @Schema(description = "The namespace of the available service.")
    private String namespace;

    @NotNull
    @NotBlank
    @Schema(description = "The icon of the available service.")
    private String icon;

    @Valid
    @NotNull
    @Schema(description = "The deployment of the managed service")
    private Deployment deployment;

    @NotNull
    @Schema(description = "The variables for the deployment, which will be passed to the deployer.")
    private List<@Valid DeployVariable> variables;

    @NotNull
    @Schema(description = "The flavors of the available service.")
    private List<@Valid Flavor> flavors;

    @NotNull
    @Schema(description = "The billing policy of the available service.")
    private Billing billing;

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
    @Schema(description = "The state of the available service.")
    private ServiceRegistrationState serviceRegistrationState;

}
