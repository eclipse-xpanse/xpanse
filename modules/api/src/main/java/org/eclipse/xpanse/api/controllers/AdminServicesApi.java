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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.cache.RedisCacheConfig;
import org.eclipse.xpanse.modules.cache.consts.CacheConstants;
import org.eclipse.xpanse.modules.database.DatabaseManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.TofuMakerManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.TerraBootManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.StackStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.observability.OpenTelemetryCollectorHealthCheck;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.security.auth.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.auth.IdentityProviderService;
import org.springframework.beans.factory.annotation.Autowired;
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
@ConditionalOnProperty(
        name = "xpanse.agent-api.enable-agent-api-only",
        havingValue = "false",
        matchIfMissing = true)
public class AdminServicesApi {

    private final TerraBootManager terraBootManager;
    private final TofuMakerManager tofuMakerManager;
    private final RedisCacheConfig redisCacheConfig;
    private final IdentityProviderManager identityProviderManager;
    private final PluginManager pluginManager;
    private final DatabaseManager databaseManager;
    private final PolicyManager policyManager;
    private final OpenTelemetryCollectorHealthCheck openTelemetryHealthCheck;

    /** Constructor method. */
    @Autowired
    public AdminServicesApi(
            @Nullable TerraBootManager terraBootManager,
            @Nullable TofuMakerManager tofuMakerManager,
            @Nullable RedisCacheConfig redisCacheConfig,
            @Nullable IdentityProviderManager identityProviderManager,
            PluginManager pluginManager,
            DatabaseManager databaseManager,
            PolicyManager policyManager,
            OpenTelemetryCollectorHealthCheck openTelemetryHealthCheck) {
        this.terraBootManager = terraBootManager;
        this.tofuMakerManager = tofuMakerManager;
        this.redisCacheConfig = redisCacheConfig;
        this.identityProviderManager = identityProviderManager;
        this.pluginManager = pluginManager;
        this.databaseManager = databaseManager;
        this.policyManager = policyManager;
        this.openTelemetryHealthCheck = openTelemetryHealthCheck;
    }

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @Operation(description = "Check health of API service and backend systems.")
    @GetMapping(value = "/stack/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN})
    @AuditApiRequest(enabled = false)
    public StackStatus stackHealthStatus() {
        StackStatus stackStatus = new StackStatus();
        stackStatus.setHealthStatus(HealthStatus.OK);
        List<BackendSystemStatus> backendSystemStatuses = getBackendSystemStatuses();
        if (!CollectionUtils.isEmpty(backendSystemStatuses)) {
            stackStatus.setBackendSystemStatuses(backendSystemStatuses);
        }
        return stackStatus;
    }

    /**
     * Method to find out the health status of the system.
     *
     * @return Returns the health status of the system.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @Operation(description = "Check only health status of API service and backend systems.")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_CSP, ROLE_ISV, ROLE_USER})
    @AuditApiRequest(enabled = false)
    public SystemStatus healthCheck() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        return systemStatus;
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
            if (Objects.nonNull(terraBootManager) && type == BackendSystemType.TERRA_BOOT) {
                BackendSystemStatus terraBootStatus = terraBootManager.getTerraBootStatus();
                if (Objects.nonNull(terraBootStatus)) {
                    backendSystemStatuses.add(terraBootStatus);
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
}
