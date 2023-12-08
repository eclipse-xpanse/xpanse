/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.manage;

import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;

/**
 * Service management request class.
 */
@Data
public class ServiceManagerRequest {

    private String userId;
    private String regionName;
    private List<DeployResourceEntity> deployResourceEntityList;

}