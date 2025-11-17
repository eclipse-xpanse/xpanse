/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.TofuMakerSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Service for managing tofu-maker. */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerManager {

    private final AdminApi openTofuAdminApi;
    private final DeploymentProperties deploymentProperties;

    @Autowired
    public TofuMakerManager(DeploymentProperties deploymentProperties, AdminApi openTofuAdminApi) {
        this.deploymentProperties = deploymentProperties;
        this.openTofuAdminApi = openTofuAdminApi;
    }

    /**
     * Get system status of TofuMaker.
     *
     * @return system status of TofuMaker.
     */
    public BackendSystemStatus getOpenTofuMakerStatus() {
        BackendSystemStatus tofuMakerStatus = new BackendSystemStatus();
        tofuMakerStatus.setBackendSystemType(BackendSystemType.TOFU_MAKER);
        tofuMakerStatus.setName(BackendSystemType.TOFU_MAKER.toValue());
        tofuMakerStatus.setEndpoint(deploymentProperties.getTofuMaker().getEndpoint());

        try {
            TofuMakerSystemStatus tofuMakerSystemStatus = openTofuAdminApi.healthCheck();
            tofuMakerStatus.setHealthStatus(
                    HealthStatus.valueOf(tofuMakerSystemStatus.getHealthStatus().getValue()));
        } catch (RestClientException e) {
            log.error("Get status of tofu-maker error:{}", e.getMessage());
            tofuMakerStatus.setHealthStatus(HealthStatus.NOK);
            tofuMakerStatus.setDetails(e.getMessage());
        }
        return tofuMakerStatus;
    }
}
