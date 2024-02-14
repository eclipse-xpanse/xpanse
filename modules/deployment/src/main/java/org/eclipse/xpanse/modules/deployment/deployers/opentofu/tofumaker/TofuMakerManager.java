/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuMakerSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Service for managing tofu-maker.
 */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerManager {

    private static final String TOFU_MAKER_PROFILE_NAME = "tofu-maker";

    @Resource
    private AdminApi openTofuAdminApi;
    @Value("${spring.profiles.active}")
    private String springProfilesActive;
    @Value("${tofu-maker.endpoint:http://localhost:9090}")
    private String openTofuMakerBaseUrl;
    @Resource
    private TofuMakerHelper tofuMakerHelper;

    /**
     * Get system status of TofuMaker.
     *
     * @return system status of TofuMaker.
     */
    public BackendSystemStatus getOpenTofuMakerStatus() {
        List<String> configSplitList = Arrays.asList(springProfilesActive.split(","));
        if (configSplitList.contains(TOFU_MAKER_PROFILE_NAME)) {
            BackendSystemStatus openTofuMakerStatus = new BackendSystemStatus();
            openTofuMakerStatus.setBackendSystemType(BackendSystemType.TOFU_MAKER);
            openTofuMakerStatus.setName(BackendSystemType.TOFU_MAKER.toValue());
            openTofuMakerStatus.setEndpoint(openTofuMakerBaseUrl);

            try {
                OpenTofuMakerSystemStatus openTofuMakerSystemStatus =
                        openTofuAdminApi.healthCheck();
                openTofuMakerStatus.setHealthStatus(HealthStatus.valueOf(
                        openTofuMakerSystemStatus.getHealthStatus().getValue()));
            } catch (RestClientException e) {
                log.error("Get status of tofu-maker error:{}", e.getMessage());
                openTofuMakerStatus.setHealthStatus(HealthStatus.NOK);
                openTofuMakerStatus.setDetails(e.getMessage());
            }
            return openTofuMakerStatus;
        }
        return null;
    }
}
