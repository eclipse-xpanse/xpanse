/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.modules.models.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.resource.Billing;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.resource.Flavor;
import org.eclipse.xpanse.modules.models.resource.Region;
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
    private List<Region> regions;

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

    @NotNull
    @Schema(description = "The variables for the deployment, which will be passed to the deployer.")
    private List<DeployVariable> variables;

    @NotNull
    @Schema(description = "The flavors of the available service.")
    private List<Flavor> flavors;

    @NotNull
    @Schema(description = "The billing policy of the available service.")
    private Billing billing;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "The registration time of the service.")
    private Date createTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "The latest update time of the available service.")
    private Date lastModifiedTime;

    @NotNull
    @Schema(description = "The state of the available service.")
    private ServiceState serviceState;

}
