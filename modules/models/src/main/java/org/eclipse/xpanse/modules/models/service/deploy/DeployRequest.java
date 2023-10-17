/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * DeployRequest model.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class DeployRequest extends DeployRequestBase {

    /**
     * The id of the service to deploy.
     */
    @Hidden
    private UUID id;
}
