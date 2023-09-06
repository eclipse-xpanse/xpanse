/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api.TerraformApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformBootSystemStatus;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin services Api.
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/xpanse")
@Secured({ROLE_ADMIN})
public class AdminServicesApi {

    @Resource
    private IdentityProviderManager identityProviderManager;

    @Resource
    private PluginManager pluginManager;

    @Value("${spring.datasource.url:jdbc:h2:file:./testdb}")
    private String dataSourceUrl;

    @Resource
    private TerraformApi terraformApi;

    @Resource
    private ApiClient apiClient;

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @Operation(description = "Check health of API service and backend systems.")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
    public SystemStatus healthCheck() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        List<BackendSystemStatus> backendSystemStatuses = checkHealthOfBackendSystem();
        if (!CollectionUtils.isEmpty(backendSystemStatuses)) {
            systemStatus.setBackendSystemStatuses(backendSystemStatuses);
        }
        return systemStatus;
    }


    /**
     * List supported cloud service provider.
     *
     * @return Returns list of cloud service provider.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @Operation(description = "List cloud service provider.")
    @GetMapping(value = "/csp", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
    public List<Csp> getCsps(
            @Parameter(name = "active",
                    description = "Whether only list cloud service provider with active plugin.")
            @RequestParam(name = "active") Boolean active) {
        if (Objects.nonNull(active) && active) {
            Set<Csp> cspSet = pluginManager.getPluginsMap().keySet();
            log.info("List cloud service provider:{} with active plugin.", cspSet);
            return cspSet.stream().sorted().toList();
        }
        return Arrays.asList(Csp.values());
    }

    private List<BackendSystemStatus> checkHealthOfBackendSystem() {
        List<BackendSystemStatus> backendSystemStatuses = new ArrayList<>();
        for (BackendSystemType type : BackendSystemType.values()) {
            if (Objects.equals(BackendSystemType.IDENTITY_PROVIDER, type)) {
                BackendSystemStatus identityProviderStatus = getIdentityProviderStatus();
                if (Objects.nonNull(identityProviderStatus)) {
                    processShownFields(identityProviderStatus);
                    backendSystemStatuses.add(identityProviderStatus);
                }
            }
            if (Objects.equals(BackendSystemType.DATABASE, type)) {
                BackendSystemStatus databaseStatus = getDatabaseStatus();
                if (Objects.nonNull(databaseStatus)) {
                    processShownFields(databaseStatus);
                    backendSystemStatuses.add(databaseStatus);
                }
            }
            if (Objects.equals(BackendSystemType.TERRAFORM_BOOT, type)) {
                BackendSystemStatus terraformBootStatus = getTerraformBootStatus();
                processShownFields(terraformBootStatus);
                backendSystemStatuses.add(terraformBootStatus);
            }
        }
        return backendSystemStatuses;
    }

    private boolean isTerraformBootApiAccessible() {
        try {
            TerraformBootSystemStatus terraformBootSystemStatus = terraformApi.healthCheck();
            return terraformBootSystemStatus.getHealthStatus()
                    .equals(TerraformBootSystemStatus.HealthStatusEnum.OK);
        } catch (Exception e) {
            return false;
        }
    }

    private BackendSystemStatus getTerraformBootStatus() {
        BackendSystemStatus terraformBootStatus = new BackendSystemStatus();
        terraformBootStatus.setBackendSystemType(BackendSystemType.TERRAFORM_BOOT);
        terraformBootStatus.setName(BackendSystemType.TERRAFORM_BOOT.toValue());
        terraformBootStatus.setEndpoint(apiClient.getBasePath());
        if (isTerraformBootApiAccessible()) {
            terraformBootStatus.setHealthStatus(HealthStatus.OK);
        } else {
            terraformBootStatus.setHealthStatus(HealthStatus.NOK);
        }
        return terraformBootStatus;
    }

    private BackendSystemStatus getIdentityProviderStatus() {
        return identityProviderManager.getActiveIdentityProviderService()
                .getIdentityProviderStatus();
    }


    private BackendSystemStatus getDatabaseStatus() {
        List<String> databaseUrlSplitList = Arrays.asList(dataSourceUrl.split(":"));
        if (databaseUrlSplitList.contains(DatabaseType.H2DB.toValue())) {
            BackendSystemStatus databaseStatus = new BackendSystemStatus();
            databaseStatus.setBackendSystemType(BackendSystemType.DATABASE);
            databaseStatus.setName(DatabaseType.H2DB.toValue());
            databaseStatus.setHealthStatus(HealthStatus.OK);
            databaseStatus.setEndpoint(dataSourceUrl);
            return databaseStatus;
        }
        if (databaseUrlSplitList.contains(DatabaseType.MARIADB.toValue())) {
            BackendSystemStatus databaseStatus = new BackendSystemStatus();
            databaseStatus.setBackendSystemType(BackendSystemType.DATABASE);
            databaseStatus.setName(DatabaseType.MARIADB.toValue());
            databaseStatus.setHealthStatus(HealthStatus.OK);
            databaseStatus.setEndpoint(dataSourceUrl);
            return databaseStatus;
        }
        return null;
    }

    private void processShownFields(BackendSystemStatus backendSystemStatus) {
        CurrentUserInfo currentUserInfo = identityProviderManager.getCurrentUserInfo();
        boolean allFieldsShown = Objects.nonNull(currentUserInfo) && !CollectionUtils.isEmpty(
                currentUserInfo.getRoles()) && currentUserInfo.getRoles().contains(ROLE_ADMIN);
        if (!allFieldsShown) {
            backendSystemStatus.setEndpoint(null);
            backendSystemStatus.setDetails(null);
        }

    }

}
