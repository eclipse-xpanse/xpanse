/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.servicestate;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;

/**
 * Service management request class.
 */
@Data
public class ServiceStateManageRequest {

    private String userId;
    private String regionName;
    private List<DeployResourceEntity> deployResourceEntityList;
    private UUID serviceId;

}