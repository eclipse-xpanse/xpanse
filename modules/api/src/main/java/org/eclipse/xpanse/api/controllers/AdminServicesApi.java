/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

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
import org.eclipse.xpanse.modules.database.DatabaseManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.TerraformBootManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.observability.OpenTelemetryCollectorHealthCheck;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.IdentityProviderService;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
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

    @Resource
    private DatabaseManager databaseManager;

    @Resource
    private TerraformBootManager terraformBootManager;

    @Resource
    private PolicyManager policyManager;

    @Resource
    private OpenTelemetryCollectorHealthCheck openTelemetryHealthCheck;


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
        List<BackendSystemStatus> backendSystemStatuses = getBackendSystemStatuses();
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


    /**
     * Get status of all active backend systems.
     *
     * @return list of system status.
     */
    private List<BackendSystemStatus> getBackendSystemStatuses() {
        List<BackendSystemStatus> systemStatuses = checkHealthOfAllBackendSystems();
        systemStatuses.forEach(this::processShownFields);
        return systemStatuses;
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
            if (type == BackendSystemType.TERRAFORM_BOOT) {
                BackendSystemStatus terraformBootStatus =
                        terraformBootManager.getTerraformBootStatus();
                if (Objects.nonNull(terraformBootStatus)) {
                    backendSystemStatuses.add(terraformBootStatus);
                }
            }
            if (type == BackendSystemType.POLICY_MAN) {
                BackendSystemStatus policyManStatus =
                        policyManager.getPolicyManStatus();
                if (Objects.nonNull(policyManStatus)) {
                    backendSystemStatuses.add(policyManStatus);
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
