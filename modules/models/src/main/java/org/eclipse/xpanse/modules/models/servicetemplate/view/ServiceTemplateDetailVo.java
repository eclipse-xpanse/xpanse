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
import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceAction;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceObject;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.springframework.hateoas.RepresentationModel;

/** Defines view object for query registered service. */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceTemplateDetailVo extends RepresentationModel<ServiceTemplateDetailVo> {

    @NotNull
    @Schema(description = "ID of the registered service.")
    private UUID serviceTemplateId;

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
    @Schema(description = "ServiceVendor of the user who registered service template.")
    private String serviceVendor;

    @NotEmpty
    @Schema(description = "The regions of the Cloud Service Provider.")
    private List<@Valid Region> regions;

    @NotNull
    @NotBlank
    @Schema(description = "The description of the registered service.")
    private String description;

    @NotNull
    @NotBlank
    @Schema(description = "The icon of the registered service.")
    private String icon;

    @Valid
    @NotNull
    @Schema(description = "The deployment of the managed service")
    private Deployment deployment;

    @NotNull
    @Schema(description = "The flavors of the registered service.")
    private @Valid FlavorsWithPrice flavors;

    @NotNull
    @Schema(description = "The billing policy of the registered service.")
    private Billing billing;

    @Valid
    @NotNull
    @Schema(
            description =
                    "Defines which cloud service account is used "
                            + "for deploying cloud resources.")
    private ServiceHostingType serviceHostingType;

    @NotNull
    @Schema(description = "createdTime of the registered service.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createdTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @Schema(description = "Last updateTime of the registered service.")
    private OffsetDateTime lastModifiedTime;

    @NotNull
    @Schema(description = "Registration state of service template.")
    private ServiceTemplateRegistrationState serviceTemplateRegistrationState;

    @NotNull
    @Schema(description = "If any request for the service template has a review in-progress.")
    private Boolean isReviewInProgress;

    @NotNull
    @Schema(description = "Is available in catalog.")
    private Boolean isAvailableInCatalog;

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
