/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.servicetemplate.view;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Billing;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorBasic;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.springframework.hateoas.RepresentationModel;

/**
 * The view model for the end-user to query the orderable services.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserOrderableServiceVo extends RepresentationModel<UserOrderableServiceVo> {

    @NotNull
    @Schema(description = "The id of the orderable service.")
    private UUID id;

    @NotNull
    @Schema(description = "The category of the orderable service.")
    private Category category;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the orderable service.")
    private String name;

    @NotNull
    @NotBlank
    @Schema(description = "The version of the orderable service.")
    private String version;

    @NotNull
    @Schema(description = "The Cloud Service Provider of the orderable service.")
    private Csp csp;

    @NotEmpty
    @Schema(description = "The regions of the Cloud Service Provider.")
    private List<@Valid Region> regions;

    @NotNull
    @NotBlank
    @Schema(description = "The description of the orderable service.")
    private String description;

    @NotNull
    @NotBlank
    @Schema(description = "The icon of the orderable service.")
    private String icon;

    @NotNull
    @Schema(description = "The variables for the deployment, which will be passed to the deployer.")
    private List<@Valid DeployVariable> variables;

    @NotNull
    @Schema(description = "The flavors of the orderable service.")
    private List<FlavorBasic> flavors;

    @NotNull
    @Schema(description = "The billing policy of the orderable service.")
    private Billing billing;

    @Valid
    @NotNull
    @Schema(description = "Defines which cloud service account is used "
            + "for deploying cloud resources.")
    private ServiceHostingType serviceHostingType;

    @NotNull
    @Schema(description = "The contact details of the service provider.")
    private ServiceProviderContactDetails serviceProviderContactDetails;

}
