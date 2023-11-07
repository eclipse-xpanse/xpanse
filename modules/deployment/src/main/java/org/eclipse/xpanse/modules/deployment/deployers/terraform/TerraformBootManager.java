/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api.TerraformApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformBootSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Service for managing terraform-boot.
 */
@Slf4j
@Component
public class TerraformBootManager {

    private static final String TERRAFORM_BOOT_PROFILE_NAME = "terraform-boot";

    @Resource
    private TerraformApi terraformApi;
    @Value("${spring.profiles.active}")
    private String springProfilesActive;
    @Value("${terraform.boot.endpoint:http://localhost:9090}")
    private String terraformBootBaseUrl;

    /**
     * Get system status of TerraformBoot.
     *
     * @return system status of TerraformBoot.
     */
    public BackendSystemStatus getTerraformBootStatus() {
        List<String> configSplitList = Arrays.asList(springProfilesActive.split(","));
        if (configSplitList.contains(TERRAFORM_BOOT_PROFILE_NAME)) {
            BackendSystemStatus terraformBootStatus = new BackendSystemStatus();
            terraformBootStatus.setBackendSystemType(BackendSystemType.TERRAFORM_BOOT);
            terraformBootStatus.setName(BackendSystemType.TERRAFORM_BOOT.toValue());
            terraformBootStatus.setEndpoint(terraformBootBaseUrl);

            try {
                TerraformBootSystemStatus terraformBootSystemStatus = terraformApi.healthCheck();
                terraformBootStatus.setHealthStatus(HealthStatus.valueOf(
                        terraformBootSystemStatus.getHealthStatus().getValue()));
            } catch (RestClientException e) {
                log.error("Get status of terraform-boot error:{}", e.getMessage());
                terraformBootStatus.setHealthStatus(HealthStatus.NOK);
                terraformBootStatus.setDetails(e.getMessage());
            }
            return terraformBootStatus;
        }
        return null;
    }
}
