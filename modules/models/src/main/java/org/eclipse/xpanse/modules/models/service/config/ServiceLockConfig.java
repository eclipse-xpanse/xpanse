/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.config;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** Service lock config model. */
@Data
public class ServiceLockConfig implements Serializable {

    @Serial private static final long serialVersionUID = 5655216229043227729L;

    /** Whether the service is locked from deletion. */
    @Schema(description = "Whether the service is locked from deletion.")
    private boolean isDestroyLocked;

    /** Whether the service is locked from modification. */
    @Schema(description = "Whether the service is locked from modification.")
    private boolean isModifyLocked;
}
