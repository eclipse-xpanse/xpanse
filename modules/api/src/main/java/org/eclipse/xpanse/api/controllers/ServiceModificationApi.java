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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceModificationAuditManager;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.modify.ServiceModificationAuditDetails;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service Management REST API.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceModificationApi {

    @Resource
    private DeployService deployService;
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;
    @Resource
    private ServiceModificationAuditManager modificationAuditManager;

    /**
     * Start a modification to modify deployed service.
     *
     * @param modifyRequest the managed service to create.
     * @return response
     */
    @Tag(name = "ServiceModification",
            description = "APIs to manage modifications of the service instances")
    @Operation(description = "Start a modification to modify the deployed service instance.")
    @PutMapping(value = "/services/modify/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public UUID modify(@Parameter(name = "serviceId", description = "id of deployed service")
                              @PathVariable("serviceId") String serviceId,
                              @Valid @RequestBody ModifyRequest modifyRequest) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(serviceId));
        boolean currentUserIsOwner =
                this.userServiceHelper.currentUserIsOwner(deployServiceEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to modify services belonging to other users.");
        }
        if (Objects.nonNull(deployServiceEntity.getLockConfig())
                && deployServiceEntity.getLockConfig().isModifyLocked()) {
            String errorMsg = "Service with id " + serviceId + " is locked from modification.";
            throw new ServiceLockedException(errorMsg);
        }

        DeployTask modifyTask =
                this.deployService.getModifyTask(modifyRequest, deployServiceEntity);
        UUID modifyTaskId = UUID.randomUUID();
        deployService.modifyService(modifyTaskId, modifyTask, deployServiceEntity);
        return modifyTaskId;
    }

    /**
     * List service modification audits by service id and task status.
     *
     * @param serviceId  id of service.
     * @param taskStatus status of the management task.
     * @return service modification audits.
     */
    @Tag(name = "ServiceModification",
            description = "APIs to manage modifications of the service instances")
    @Operation(description = "List modification audits of the service instance")
    @GetMapping(value = "/services/{serviceId}/modifications", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public List<ServiceModificationAuditDetails> listServiceModificationAudits(
            @Parameter(name = "serviceId", description = "id of the service")
            @PathVariable(name = "serviceId") String serviceId,
            @Parameter(name = "taskStatus", description = "status of the modification")
            @RequestParam(name = "taskStatus", required = false) TaskStatus taskStatus) {
        return modificationAuditManager.listServiceModificationAudits(
                UUID.fromString(serviceId), taskStatus);
    }

    /**
     * Delete all state management modifications of the service.
     *
     * @param serviceId id of the service.
     */
    @Tag(name = "ServiceModification",
            description = "APIs to manage modifications of the service instances")
    @Operation(description = "Delete all state management modifications of the service.")
    @DeleteMapping(value = "/services/{serviceId}/modifications",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public void deleteAuditsByServiceId(
            @Parameter(name = "serviceId", description = "id of the service")
            @PathVariable(name = "serviceId") String serviceId) {

        modificationAuditManager.deleteAuditsByServiceId(UUID.fromString(serviceId));
    }


    /**
     * Get modification audit details by the modification id.
     *
     * @param modificationId id of the modification.
     * @return modification audit details.
     */
    @Tag(name = "ServiceModification",
            description = "APIs to manage modifications of the service instances")
    @Operation(description = "Get modification audit details by the modification id.")
    @GetMapping(value = "/services/modifications/{modificationId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromModificationAuditId")
    public ServiceModificationAuditDetails getAuditDetailsByModificationId(
            @Parameter(name = "modificationId", description = "id of the modification audit")
            @PathVariable(name = "modificationId") String modificationId) {
        return modificationAuditManager.getAuditDetailsByModificationId(
                UUID.fromString(modificationId));
    }

    /**
     * Delete service modification audit by the modification id.
     *
     * @param modificationId id of the service.
     */
    @Tag(name = "ServiceModification",
            description = "APIs to manage modifications of the service instances")
    @Operation(description = "Delete service modification audit by the modification id.")
    @DeleteMapping(value = "/services/modifications/{modificationId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromModificationAuditId")
    public void deleteAuditByModificationId(
            @Parameter(name = "modificationId", description = "id of the modification audit")
            @PathVariable(name = "modificationId") String modificationId) {
        modificationAuditManager.deleteAuditByModificationId(UUID.fromString(modificationId));
    }
}