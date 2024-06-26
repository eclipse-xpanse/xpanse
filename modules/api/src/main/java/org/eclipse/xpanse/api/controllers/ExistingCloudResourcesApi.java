/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface methods for managing cloud resources.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ExistingCloudResourcesApi {

    @Resource
    private PluginManager pluginManager;

    @Resource
    private UserServiceHelper userServiceHelper;

    /**
     * List existing cloud resources based on type.
     */
    @Tag(name = "CloudResources",
            description = "API to view cloud resources by type")
    @GetMapping(value = "/csp/resources/{deployResourceKind}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List existing cloud resource names with kind")
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public List<String> getExistingResourceNamesWithKind(
            @Parameter(name = "csp", description = "name of the cloud service provider")
            @RequestParam(name = "csp") Csp csp,
            @Parameter(name = "region", description = "name of he region")
            @RequestParam(name = "region") String region,
            @Parameter(name = "deployResourceKind", description = "kind of the CloudResource")
            @PathVariable("deployResourceKind") DeployResourceKind deployResourceKind,
            @Parameter(name = "serviceId", description = "id of the deployed service")
            @RequestParam(name = "serviceId", required = false) String serviceId) {
        UUID uuid = StringUtils.isBlank(serviceId) ? null : UUID.fromString(serviceId);
        String userId = userServiceHelper.getCurrentUserId();
        OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
        return orchestratorPlugin.getExistingResourceNamesWithKind(userId, region,
                deployResourceKind, uuid);
    }
}
