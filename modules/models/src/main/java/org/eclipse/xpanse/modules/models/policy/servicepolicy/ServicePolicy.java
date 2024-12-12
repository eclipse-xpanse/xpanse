/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.policy.servicepolicy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/** Define view object for detail of the service policy entity. */
@Data
public class ServicePolicy {

    /** The id of the entity. */
    @NotNull
    @Schema(description = "The id of the policy.")
    private UUID servicePolicyId;

    /** The valid policy created by the user. */
    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The valid policy belongs to the service.")
    private String policy;

    /** The id of registered service template which the policy belongs to. */
    @NotNull
    @Schema(description = "The id of registered service template which the policy belongs to.")
    private UUID serviceTemplateId;

    /** The flavor name list which the policy belongs to. */
    @Schema(
            description =
                    "The flavor name list which the policy belongs to. If the list is empty, then"
                            + " the policy will be executed for during service deployment of all"
                            + " flavors.")
    private List<String> flavorNameList;

    /** Is the policy enabled. */
    @NotNull
    @Schema(description = "Is the policy enabled.")
    private Boolean enabled;

    @NotNull
    @Schema(description = "Time of the policy created.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createTime;

    @NotNull
    @Schema(description = "Time of the policy updated.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime lastModifiedTime;
}
