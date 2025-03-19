/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraBootSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Service for managing terra-boot. */
@Slf4j
@Component
@Profile("terra-boot")
public class TerraBootManager {

    private static final String TERRA_BOOT_PROFILE_NAME = "terra-boot";

    @Resource private AdminApi terraformAdminApi;

    @Value("${spring.profiles.active}")
    private String springProfilesActive;

    @Value("${terra-boot.endpoint:http://localhost:9090}")
    private String terraBootBaseUrl;

    /**
     * Get system status of TerraBoot.
     *
     * @return system status of TerraBoot.
     */
    public BackendSystemStatus getTerraBootStatus() {
        List<String> configSplitList = Arrays.asList(springProfilesActive.split(","));
        if (configSplitList.contains(TERRA_BOOT_PROFILE_NAME)) {
            BackendSystemStatus terraBootStatus = new BackendSystemStatus();
            terraBootStatus.setBackendSystemType(BackendSystemType.TERRA_BOOT);
            terraBootStatus.setName(BackendSystemType.TERRA_BOOT.toValue());
            terraBootStatus.setEndpoint(terraBootBaseUrl);

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
        return null;
    }
}
