/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraBootSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Service for managing terra-boot. */
@Slf4j
@Component
@Profile("terra-boot")
public class TerraBootManager {

    private static final String TERRA_BOOT_PROFILE_NAME = "terra-boot";

    private final AdminApi terraformAdminApi;

    private final DeploymentProperties deploymentProperties;

    public TerraBootManager(AdminApi terraformAdminApi, DeploymentProperties deploymentProperties) {
        this.terraformAdminApi = terraformAdminApi;
        this.deploymentProperties = deploymentProperties;
    }

    /**
     * Get system status of TerraBoot.
     *
     * @return system status of TerraBoot.
     */
    public BackendSystemStatus getTerraBootStatus() {
        BackendSystemStatus terraBootStatus = new BackendSystemStatus();
        terraBootStatus.setBackendSystemType(BackendSystemType.TERRA_BOOT);
        terraBootStatus.setName(BackendSystemType.TERRA_BOOT.toValue());
        terraBootStatus.setEndpoint(deploymentProperties.getTerraBoot().getEndpoint());

        try {
            TerraBootSystemStatus terraBootSystemStatus = terraformAdminApi.healthCheck();
            terraBootStatus.setHealthStatus(
                    HealthStatus.valueOf(terraBootSystemStatus.getHealthStatus().getValue()));
        } catch (RestClientException e) {
            log.error("Get status of terra-boot error:{}", e.getMessage());
            terraBootStatus.setHealthStatus(HealthStatus.NOK);
            terraBootStatus.setDetails(e.getMessage());
        }
        return terraBootStatus;
    }
}
