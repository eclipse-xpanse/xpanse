/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface methods for Service Migration.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceMigrationApi {

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    private IdentityProviderManager identityProviderManager;

    @Resource
    private WorkflowUtils workflowUtils;

    /**
     * Create a job to migrate the deployed service.
     *
     * @return response
     */
    @Tag(name = "Migration", description = "APIs to manage the service migration.")
    @Operation(description = "Create a job to migrate the deployed service.")
    @PostMapping(value = "/services/migration", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID migrate(@Valid @RequestBody MigrateRequest migrateRequest) {
        String userId = getUserId(migrateRequest.getId());
        Map<String, Object> variable =
                getMigrateProcessVariable(migrateRequest, UUID.randomUUID(), userId);
        ProcessInstance instance =
                workflowUtils.startProcess(MigrateConstants.PROCESS_KEY, variable);
        return UUID.fromString(instance.getProcessInstanceId());
    }

    private String getUserId(UUID migrationId) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(migrationId);
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        String userId = userIdOptional.orElse(null);
        if (!StringUtils.equals(userId, deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to migrate services belonging to other users.");
        }
        return userId;
    }

    private Map<String, Object> getMigrateProcessVariable(MigrateRequest migrateRequest,
            UUID newServiceId, String userId) {
        Map<String, Object> variable = new HashMap<>();
        variable.put(MigrateConstants.ID, migrateRequest.getId());
        variable.put(MigrateConstants.NEW_ID, newServiceId);
        variable.put(MigrateConstants.MIGRATE_REQUEST, migrateRequest);
        variable.put(MigrateConstants.USER_ID, userId);
        return variable;
    }

}
