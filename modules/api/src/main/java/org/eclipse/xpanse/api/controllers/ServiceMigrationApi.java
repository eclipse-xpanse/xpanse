/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.EulaNotAccepted;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.eclipse.xpanse.modules.models.workflow.migrate.view.ServiceMigrationDetails;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceMigrationApi {

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    private UserServiceHelper userServiceHelper;

    @Resource
    private WorkflowUtils workflowUtils;

    @Resource
    private MigrationService migrationService;

    /**
     * Create a job to migrate the deployed service.
     *
     * @return response
     */
    @Tag(name = "Migration", description = "APIs to manage the service migration.")
    @Operation(description = "Create a job to migrate the deployed service.")
    @PostMapping(value = "/services/migration", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public UUID migrate(@Valid @RequestBody MigrateRequest migrateRequest) {
        validateData(migrateRequest);
        ServiceDeploymentEntity serviceDeploymentEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(
                        migrateRequest.getOriginalServiceId());
        String userId = getUserId();
        if (!StringUtils.equals(userId, serviceDeploymentEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to migrate services belonging to other users.");
        }
        if (Objects.nonNull(serviceDeploymentEntity.getLockConfig())
                && serviceDeploymentEntity.getLockConfig().isModifyLocked()) {
            String errorMsg = String.format("Service with id %s is locked from migration.",
                    migrateRequest.getOriginalServiceId());
            throw new ServiceLockedException(errorMsg);
        }
        Map<String, Object> variable =
                getMigrateProcessVariable(migrateRequest, UUID.randomUUID(), userId);
        ProcessInstance instance =
                workflowUtils.startProcess(MigrateConstants.PROCESS_KEY, variable);
        return UUID.fromString(instance.getProcessInstanceId());
    }

    private void validateData(MigrateRequest migrateRequest) {
        ServiceTemplateEntity searchServiceTemplate = new ServiceTemplateEntity();
        searchServiceTemplate.setName(StringUtils.lowerCase(migrateRequest.getServiceName()));
        searchServiceTemplate.setVersion(StringUtils.lowerCase(migrateRequest.getVersion()));
        searchServiceTemplate.setCsp(migrateRequest.getCsp());
        searchServiceTemplate.setCategory(migrateRequest.getCategory());
        searchServiceTemplate.setServiceHostingType(migrateRequest.getServiceHostingType());
        ServiceTemplateEntity existingServiceTemplate =
                migrationService.findServiceTemplate(searchServiceTemplate);
        if (Objects.nonNull(existingServiceTemplate)
                && StringUtils.isNotBlank(existingServiceTemplate.getOcl().getEula())
                && !migrateRequest.isEulaAccepted()) {
            log.error("Service not accepted Eula.");
            throw new EulaNotAccepted("Service not accepted Eula.");
        }
        if (!existingServiceTemplate.getOcl().getBilling().getBillingModes()
                .contains(migrateRequest.getBillingMode())) {
            String errorMsg = String.format(
                    "The service template with id %s does not support billing mode %s.",
                    existingServiceTemplate.getId(), migrateRequest.getBillingMode());
            log.error(errorMsg);
            throw new BillingModeNotSupported(errorMsg);
        }
    }

    /**
     * List all services migration by a user.
     *
     * @param migrationId     ID of the service migrate.
     * @param newServiceId    ID of the new service.
     * @param oldServiceId    ID of the old service.
     * @param migrationStatus Status of the service migrate.
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Migration", description = "APIs to manage the service migration.")
    @Operation(description = "List all services migration by a user.")
    @GetMapping(value = "/services/migrations", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceMigrationId")
    public List<ServiceMigrationDetails> listServiceMigrations(
            @Parameter(name = "migrationId", description = "Id of the service migrate")
            @RequestParam(name = "migrationId", required = false) UUID migrationId,
            @Parameter(name = "newServiceId", description = "Id of the new service")
            @RequestParam(name = "newServiceId", required = false) UUID newServiceId,
            @Parameter(name = "oldServiceId", description = "Id of the old service")
            @RequestParam(name = "oldServiceId", required = false) UUID oldServiceId,
            @Parameter(name = "migrationStatus", description = "Status of the service migrate")
            @RequestParam(name = "migrationStatus", required = false)
            MigrationStatus migrationStatus
    ) {
        return migrationService.listServiceMigrations(migrationId, newServiceId, oldServiceId,
                migrationStatus, getUserId());
    }

    /**
     * Get migration records based on migration id.
     *
     * @param migrationId ID of the service migrate.
     * @return serviceMigrationEntity.
     */
    @Tag(name = "Migration", description = "APIs to manage the service migration.")
    @Operation(description = "Get migration records based on migration id.")
    @GetMapping(value = "/services/migration/{migrationId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceMigrationId")
    public ServiceMigrationDetails getMigrationOrderDetailsById(
            @Parameter(name = "migrationId", description = "Migration ID")
            @PathVariable("migrationId") String migrationId) {
        return migrationService.getMigrationOrderDetails(UUID.fromString(migrationId), getUserId());
    }

    private String getUserId() {
        return userServiceHelper.getCurrentUserId();
    }

    private Map<String, Object> getMigrateProcessVariable(MigrateRequest migrateRequest,
                                                          UUID newServiceId, String userId) {
        Map<String, Object> variable = new HashMap<>();
        variable.put(MigrateConstants.ID, migrateRequest.getOriginalServiceId());
        variable.put(MigrateConstants.NEW_ID, newServiceId);
        variable.put(MigrateConstants.MIGRATE_REQUEST, migrateRequest);
        variable.put(MigrateConstants.USER_ID, userId);
        return variable;
    }

}
