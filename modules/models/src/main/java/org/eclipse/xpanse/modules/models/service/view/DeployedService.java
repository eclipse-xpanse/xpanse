/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;

/** Define view object for UI Client to query deployed services. */
@Data
public class DeployedService {

    @NotNull
    @Schema(description = "The ID of the service")
    private UUID serviceId;

    @NotNull
    @Schema(description = "The catalog of the service")
    private Category category;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the service")
    private String name;

    @Schema(
            description =
                    "Customer's name for the service. Used only for customer's reference."
                            + "If not provided, this value will be auto-generated")
    private String customerServiceName;

    @NotNull
    @NotBlank
    @Schema(description = "The version of the service")
    private String version;

    @NotNull
    @Schema(description = "The provider of the service")
    private Csp csp;

    @NotNull
    @Schema(
            description =
                    "Defines which cloud service account is used "
                            + "for deploying cloud resources.")
    private ServiceHostingType serviceHostingType;

    @NotNull
    @Schema(description = "The region of the service.")
    private Region region;

    @Schema(description = "The availability zones of the service")
    private Map<String, String> availabilityZones;

    @NotNull
    @NotBlank
    @Schema(description = "The flavor of the service")
    private String flavor;

    @NotNull
    @Schema(description = "The billing mode of the managed service.")
    private BillingMode billingMode;

    @Schema(description = "The input properties to deploy the service.")
    private Map<String, String> inputProperties;

    @NotNull
    @Schema(description = "The id of the Service Template")
    private UUID serviceTemplateId;

    @Schema(description = "The id of the user who deployed the service.")
    private String userId;

    @NotNull
    @Schema(description = "The deployment state of the service")
    private ServiceDeploymentState serviceDeploymentState;

    @NotNull
    @Schema(description = "The run state of the service")
    private ServiceState serviceState = ServiceState.NOT_RUNNING;

    @NotNull
    @Schema(description = "Time of register service.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createTime;

    @NotNull
    @Schema(description = "Time of update service.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime lastModifiedTime;

    @Schema(description = "Time of start service.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime lastStartedAt;

    @Schema(description = "Time of stop service.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime lastStoppedAt;

    @Schema(description = "The locks whether the service can be modified or destroyed.")
    private ServiceLockConfig lockConfig;

    @Schema(description = "Details of the service configuration.")
    private ServiceConfigurationDetails serviceConfigurationDetails;
}
