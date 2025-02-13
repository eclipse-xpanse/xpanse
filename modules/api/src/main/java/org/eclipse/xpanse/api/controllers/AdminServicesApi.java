/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_CSP;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.cache.RedisCacheConfig;
import org.eclipse.xpanse.modules.cache.consts.CacheConstants;
import org.eclipse.xpanse.modules.database.DatabaseManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.TofuMakerManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.TerraformBootManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.observability.OpenTelemetryCollectorHealthCheck;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.security.auth.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.auth.IdentityProviderService;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Admin services Api. */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/xpanse")
@Secured({ROLE_ADMIN})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class AdminServicesApi {

    private final TerraformBootManager terraformBootManager;
    private final TofuMakerManager tofuMakerManager;
    private final RedisCacheConfig redisCacheConfig;
    @Resource private IdentityProviderManager identityProviderManager;
    @Resource private PluginManager pluginManager;
    @Resource private DatabaseManager databaseManager;
    @Resource private PolicyManager policyManager;
    @Resource private OpenTelemetryCollectorHealthCheck openTelemetryHealthCheck;
    @Resource private UserServiceHelper userServiceHelper;

    /** Constructor for AdminServicesApi bean. */
    public AdminServicesApi(
            @Nullable TerraformBootManager terraformBootManager,
            @Nullable TofuMakerManager tofuMakerManager,
            @Nullable RedisCacheConfig redisCacheConfig) {
        this.terraformBootManager = terraformBootManager;
        this.tofuMakerManager = tofuMakerManager;
        this.redisCacheConfig = redisCacheConfig;
    }

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @Operation(description = "Check health of API service and backend systems.")
    @GetMapping(value = "/health/stack", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN})
    @AuditApiRequest(enabled = false)
    public SystemStatus stackHealthStatus() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        List<BackendSystemStatus> backendSystemStatuses = getBackendSystemStatuses();
        if (!CollectionUtils.isEmpty(backendSystemStatuses)) {
            systemStatus.setBackendSystemStatuses(backendSystemStatuses);
        }
        return systemStatus;
    }

    /**
     * List supported backend systems.
     *
     * @return Returns list of backend systems {name,healthstatus}.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @Operation(description = "Get name and status of backend systems")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_CSP, ROLE_ISV, ROLE_USER})
    @AuditApiRequest(enabled = false)
    public List<BackendSystemHealthInfo> healthCheck() {

        return getSystemNameAndHealthInfo();
    }

    /**
     * List supported cloud service provider.
     *
     * @return Returns list of cloud service provider.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @Operation(description = "List cloud service providers with active plugin.")
    @GetMapping(value = "/csps/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_CSP, ROLE_ISV, ROLE_USER})
    @AuditApiRequest(enabled = false)
    public List<Csp> getActiveCsps() {
        Set<Csp> cspSet = pluginManager.getPluginsMap().keySet();
        log.info("Cloud service providers:{} with active plugins.", cspSet);
        return cspSet.stream().sorted().toList();
    }

    /**
     * Get status of all active backend systems.
     *
     * @return list of system status.
     */
    private List<BackendSystemStatus> getBackendSystemStatuses() {
        return checkHealthOfAllBackendSystems();
    }

    private List<BackendSystemStatus> checkHealthOfAllBackendSystems() {
        List<BackendSystemStatus> backendSystemStatuses = new ArrayList<>();
        for (BackendSystemType type : BackendSystemType.values()) {
            if (type == BackendSystemType.IDENTITY_PROVIDER) {
                IdentityProviderService identityProviderService =
                        identityProviderManager.getActiveIdentityProviderService();
                if (Objects.nonNull(identityProviderService)) {
                    BackendSystemStatus identityProviderStatus =
                            identityProviderService.getIdentityProviderStatus();
                    if (Objects.nonNull(identityProviderStatus)) {
                        backendSystemStatuses.add(identityProviderStatus);
                    }
                }
            }
            if (type == BackendSystemType.DATABASE) {
                BackendSystemStatus databaseStatus = databaseManager.getDatabaseStatus();
                if (Objects.nonNull(databaseStatus)) {
                    backendSystemStatuses.add(databaseStatus);
                }
            }
            if (Objects.nonNull(terraformBootManager) && type == BackendSystemType.TERRAFORM_BOOT) {
                BackendSystemStatus terraformBootStatus =
                        terraformBootManager.getTerraformBootStatus();
                if (Objects.nonNull(terraformBootStatus)) {
                    backendSystemStatuses.add(terraformBootStatus);
                }
            }
            if (Objects.nonNull(tofuMakerManager) && type == BackendSystemType.TOFU_MAKER) {
                BackendSystemStatus openTofuMakerStatus = tofuMakerManager.getOpenTofuMakerStatus();
                if (Objects.nonNull(openTofuMakerStatus)) {
                    backendSystemStatuses.add(openTofuMakerStatus);
                }
            }
            if (type == BackendSystemType.POLICY_MAN) {
                BackendSystemStatus policyManStatus = policyManager.getPolicyManStatus();
                if (Objects.nonNull(policyManStatus)) {
                    backendSystemStatuses.add(policyManStatus);
                }
            }
            if (type == BackendSystemType.CACHE_PROVIDER) {
                if (Objects.nonNull(redisCacheConfig)) {
                    BackendSystemStatus redisCacheStatus = redisCacheConfig.getRedisCacheStatus();
                    if (Objects.nonNull(redisCacheStatus)) {
                        backendSystemStatuses.add(redisCacheStatus);
                    }
                } else {
                    BackendSystemStatus cacheStatus = new BackendSystemStatus();
                    cacheStatus.setBackendSystemType(BackendSystemType.CACHE_PROVIDER);
                    cacheStatus.setHealthStatus(HealthStatus.OK);
                    cacheStatus.setName(CacheConstants.CACHE_PROVIDER_CAFFEINE);
                    cacheStatus.setEndpoint(CacheConstants.CACHE_PROVIDER_CAFFEINE_ENDPOINT);
                    backendSystemStatuses.add(cacheStatus);
                }
            }
            if (type == BackendSystemType.OPEN_TELEMETRY_COLLECTOR) {
                BackendSystemStatus otelExporterStatus =
                        openTelemetryHealthCheck.getOpenTelemetryHealthStatus();
                if (Objects.nonNull(otelExporterStatus)) {
                    backendSystemStatuses.add(otelExporterStatus);
                }
            }
        }
        return backendSystemStatuses;
    }

    private List<BackendSystemHealthInfo> getSystemNameAndHealthInfo(){
        List<BackendSystemHealthInfo> backendSystemHealthInfos=new ArrayList<>();
        for (BackendSystemType type : BackendSystemType.values()) {
            if (type == BackendSystemType.IDENTITY_PROVIDER) {
                IdentityProviderService identityProviderService =
                        identityProviderManager.getActiveIdentityProviderService();
                if (Objects.nonNull(identityProviderService)) {
                    BackendSystemStatus identityProviderStatus =
                            identityProviderService.getIdentityProviderStatus();
                    if (Objects.nonNull(identityProviderStatus)) {
                        backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                                .healthStatus(identityProviderStatus.getHealthStatus()).name(identityProviderStatus.getName()).build());
                    }
                }
            }
            if (type == BackendSystemType.DATABASE) {
                BackendSystemStatus databaseStatus = databaseManager.getDatabaseStatus();
                if (Objects.nonNull(databaseStatus)) {
                    backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                            .healthStatus(databaseStatus.getHealthStatus()).name(databaseStatus.getName()).build());
                }
            }
            if (Objects.nonNull(terraformBootManager) && type == BackendSystemType.TERRAFORM_BOOT) {
                BackendSystemStatus terraformBootStatus =
                        terraformBootManager.getTerraformBootStatus();
                if (Objects.nonNull(terraformBootStatus)) {
                   backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                           .healthStatus(terraformBootStatus.getHealthStatus()).name(terraformBootStatus.getName()).build());
                }
            }
            if (Objects.nonNull(tofuMakerManager) && type == BackendSystemType.TOFU_MAKER) {
                BackendSystemStatus openTofuMakerStatus = tofuMakerManager.getOpenTofuMakerStatus();
                if (Objects.nonNull(openTofuMakerStatus)) {
                    backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                            .healthStatus(openTofuMakerStatus.getHealthStatus()).name(openTofuMakerStatus.getName()).build());
                }
            }
            if (type == BackendSystemType.POLICY_MAN) {
                BackendSystemStatus policyManStatus = policyManager.getPolicyManStatus();
                if (Objects.nonNull(policyManStatus)) {
                    backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                            .healthStatus(policyManStatus.getHealthStatus()).name(policyManStatus.getName()).build());
                }
            }
            if (type == BackendSystemType.CACHE_PROVIDER) {
                if (Objects.nonNull(redisCacheConfig)) {
                    BackendSystemStatus redisCacheStatus = redisCacheConfig.getRedisCacheStatus();
                    if (Objects.nonNull(redisCacheStatus)) {
                        backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                                .healthStatus(redisCacheStatus.getHealthStatus()).name(redisCacheStatus.getName()).build());
                    }
                } else {
                    backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                            .healthStatus(HealthStatus.OK).name(CacheConstants.CACHE_PROVIDER_CAFFEINE).build());
                }
            }
            if (type == BackendSystemType.OPEN_TELEMETRY_COLLECTOR) {
                BackendSystemStatus otelExporterStatus =
                        openTelemetryHealthCheck.getOpenTelemetryHealthStatus();
                if (Objects.nonNull(otelExporterStatus)) {
                    backendSystemHealthInfos.add(BackendSystemHealthInfo.builder()
                            .healthStatus(otelExporterStatus.getHealthStatus()).name(otelExporterStatus.getName()).build());
                }
            }
        }
        return backendSystemHealthInfos;
    }


    @Builder
    static class BackendSystemHealthInfo{
        String name;
        HealthStatus healthStatus;
    }
}
