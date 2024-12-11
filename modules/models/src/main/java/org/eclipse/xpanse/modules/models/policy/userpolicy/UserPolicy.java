/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.policy.userpolicy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Csp;

/** Define view object for detail of the policy entity. */
@Data
public class UserPolicy {

    /** The id of the entity. */
    @NotNull
    @Schema(description = "The id of the policy.")
    private UUID userPolicyId;

    /** The valid policy created by the user. */
    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The valid policy created by the user.")
    private String policy;

    /** The csp which the policy belongs to. */
    @NotNull
    @Schema(description = "The csp which the policy belongs to.")
    private Csp csp;

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
