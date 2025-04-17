/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.validators.DeploymentConfigurationConstraint;

/** Defines for OCLv2. */
@Valid
@Data
@Slf4j
public class Ocl implements Serializable {

    @Serial private static final long serialVersionUID = -51411975788603138L;

    @Valid
    @NotNull
    @Schema(description = "The catalog of the service")
    private Category category;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The version of the Ocl")
    private String version;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the managed service")
    private String name;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The version of the managed service")
    private String serviceVersion;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The description of the managed service")
    private String description;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The serviceVendor of the managed service")
    private String serviceVendor;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The icon of the managed service")
    private String icon;

    @Valid
    @NotNull
    @Schema(description = "The cloud service provider of the managed service")
    private CloudServiceProvider cloudServiceProvider;

    @Valid
    @NotNull
    @DeploymentConfigurationConstraint
    @Schema(description = "The deployment of the managed service")
    private Deployment deployment;

    @Valid
    @NotNull
    @Schema(description = "The flavors of the managed service")
    private FlavorsWithPrice flavors;

    @Valid
    @NotNull
    @Schema(description = "The billing policy of the managed service")
    private Billing billing;

    @Valid
    @NotNull
    @Schema(
            description =
                    "Defines which cloud service account is used "
                            + "for deploying cloud resources.")
    private ServiceHostingType serviceHostingType;

    @Valid
    @NotNull
    @Schema(description = "The contact details of the service provider.")
    private ServiceProviderContactDetails serviceProviderContactDetails;

    @Schema(description = "End user license agreement content of the service.")
    private String eula;

    @Schema(description = "manage service configuration.")
    private ServiceChangeManage serviceConfigurationManage;

    @Schema(description = "manage service action.")
    private List<ServiceAction> serviceActions;

    @Schema(description = "manage service object.")
    private List<ServiceObject> serviceObjects;
}
