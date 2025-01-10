/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment;

import io.swagger.v3.oas.annotations.Hidden;
import java.io.Serial;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** DeployRequest model. */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class DeployRequest extends DeployRequestBase {

    @Serial private static final long serialVersionUID = -8027459207480627100L;

    /** The id of the service to deploy. */
    @Hidden private UUID serviceId;
}
