/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicemodification.DatabaseServiceModificationAuditStorage;
import org.eclipse.xpanse.modules.database.servicemodification.ServiceModificationAuditEntity;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.modify.ServiceModificationAuditDetails;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to manage service modification audit.
 */
@Component
public class ServiceModificationAuditManager {

    @Resource
    private DatabaseServiceModificationAuditStorage modificationAuditStorage;
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;
    @Resource
    private UserServiceHelper userServiceHelper;

    /**
     * Create new modification audit.
     *
     * @param modificationId      id of the modification
     * @param modifyTask          modification modification data
     * @param deployServiceEntity deployed service entity
     * @return DB entity of the modification audit
     */
    public ServiceModificationAuditEntity createNewModificationAudit(
            UUID modificationId, DeployTask modifyTask, DeployServiceEntity deployServiceEntity) {
        ServiceModificationAuditEntity modificationAudit = new ServiceModificationAuditEntity();
        modificationAudit.setId(modificationId);
        modificationAudit.setServiceId(deployServiceEntity.getId());
        modificationAudit.setPreviousDeployRequest(deployServiceEntity.getDeployRequest());
        modificationAudit.setNewDeployRequest(modifyTask.getDeployRequest());
        modificationAudit.setTaskStatus(TaskStatus.CREATED);
        modificationAudit.setPreviousDeployedResources(
                EntityTransUtils.transToDeployResourceList(
                        deployServiceEntity.getDeployResourceList()));
        modificationAudit.setPreviousDeployedResultProperties(
                new HashMap<>(deployServiceEntity.getPrivateProperties()));
        modificationAudit.setPreviousDeployedServiceProperties(
                new HashMap<>(deployServiceEntity.getProperties()));
        return modificationAuditStorage.storeAndFlush(modificationAudit);
    }

    /**
     * Start modification progress by id.
     *
     * @param modificationId id of the modification
     */
    public void startModificationProgressById(UUID modificationId) {
        ServiceModificationAuditEntity modificationAudit =
                modificationAuditStorage.getEntityById(modificationId);
        modificationAudit.setTaskStatus(TaskStatus.IN_PROGRESS);
        modificationAudit.setStartedTime(OffsetDateTime.now());
        modificationAuditStorage.storeAndFlush(modificationAudit);
    }

    /**
     * Update modification audit by the deployment result.
     *
     * @param storedEntity DB entity to be updated
     * @param deployResult Deployment Result.
     */
    public void updateModificationAuditWithDeployResult(
            ServiceModificationAuditEntity storedEntity, DeployResult deployResult) {
        if (Objects.nonNull(storedEntity)) {
            ServiceModificationAuditEntity entityToUpdate = new ServiceModificationAuditEntity();
            BeanUtils.copyProperties(storedEntity, entityToUpdate);
            if (Objects.isNull(deployResult.getState())) {
                storedEntity.setTaskStatus(TaskStatus.IN_PROGRESS);
                storedEntity.setStartedTime(OffsetDateTime.now());
            } else {
                if (deployResult.getState() == DeployerTaskStatus.MODIFICATION_FAILED) {
                    storedEntity.setTaskStatus(TaskStatus.FAILED);
                    storedEntity.setErrorMsg(deployResult.getMessage());
                    storedEntity.setCompletedTime(OffsetDateTime.now());
                } else if (deployResult.getState() == DeployerTaskStatus.MODIFICATION_SUCCESSFUL) {
                    storedEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
                    storedEntity.setCompletedTime(OffsetDateTime.now());
                }
            }
        }
    }


    /**
     * List the service modification audits.
     *
     * @param serviceId          service id.
     * @param modificationStatus modification status.
     * @return list of service modification audits.
     */
    public List<ServiceModificationAuditDetails> listServiceModificationAudits(
            UUID serviceId, TaskStatus modificationStatus) {
        DeployServiceEntity deployedService = getDeployServiceEntity(serviceId);
        ServiceModificationAuditEntity query = new ServiceModificationAuditEntity();
        query.setServiceId(deployedService.getId());
        query.setTaskStatus(modificationStatus);
        List<ServiceModificationAuditEntity> modificationEntities =
                modificationAuditStorage.queryEntities(query);
        return modificationEntities.stream()
                .map(EntityTransUtils::transToServiceModificationAuditDetails)
                .toList();
    }


    /**
     * Delete the service modification audits by the service id.
     *
     * @param serviceId service id.
     */
    public void deleteAuditsByServiceId(UUID serviceId) {
        DeployServiceEntity deployService = getDeployServiceEntity(serviceId);
        ServiceModificationAuditEntity query = new ServiceModificationAuditEntity();
        query.setServiceId(deployService.getId());
        List<ServiceModificationAuditEntity> modificationEntities =
                modificationAuditStorage.queryEntities(query);
        modificationAuditStorage.deleteBatch(modificationEntities);
    }


    /**
     * Get the service modification audit details with the modification id.
     *
     * @param modificationId modification id.
     * @return service modification audit details.
     */
    public ServiceModificationAuditDetails getAuditDetailsByModificationId(UUID modificationId) {
        ServiceModificationAuditEntity modificationEntity = getModificationEntity(modificationId);
        return EntityTransUtils.transToServiceModificationAuditDetails(modificationEntity);
    }

    /**
     * Get the latest service modification audit with the service id.
     *
     * @param serviceId service id.
     * @return service modification audit details.
     */
    public ServiceModificationAuditDetails getLatestModificationAudit(UUID serviceId) {
        ServiceModificationAuditEntity query = new ServiceModificationAuditEntity();
        query.setServiceId(serviceId);
        List<ServiceModificationAuditEntity> modificationEntities =
                modificationAuditStorage.queryEntities(query);
        if (!CollectionUtils.isEmpty(modificationEntities)) {
            return EntityTransUtils.transToServiceModificationAuditDetails(
                    modificationEntities.getFirst());
        }
        return null;
    }

    /**
     * Delete the service modification audit with the modification id.
     *
     * @param modificationId modification id.
     */
    public void deleteAuditByModificationId(UUID modificationId) {
        ServiceModificationAuditEntity modificationEntity = getModificationEntity(modificationId);
        modificationAuditStorage.delete(modificationEntity);
    }

    private DeployServiceEntity getDeployServiceEntity(UUID serviceId) {
        DeployServiceEntity deployedService = deployServiceStorage.findDeployServiceById(serviceId);
        if (Objects.nonNull(deployedService)) {
            if (isNotOwnerOrAdminUser(deployedService)) {
                String errorMsg = "No permissions to manage service modification audits of "
                        + "the services belonging to other users";
                throw new AccessDeniedException(errorMsg);
            }
        }
        return deployedService;
    }

    private ServiceModificationAuditEntity getModificationEntity(UUID modificationId) {
        ServiceModificationAuditEntity modificationEntity =
                modificationAuditStorage.getEntityById(modificationId);
        if (Objects.nonNull(modificationEntity)) {
            DeployServiceEntity deployedService =
                    deployServiceStorage.findDeployServiceById(modificationEntity.getServiceId());
            if (Objects.nonNull(deployedService)) {
                if (isNotOwnerOrAdminUser(deployedService)) {
                    String errorMsg = "No permissions to manage service modification audit of "
                            + "the services belonging to other users";
                    throw new AccessDeniedException(errorMsg);
                }
            }
        }
        return modificationEntity;
    }


    private boolean isNotOwnerOrAdminUser(DeployServiceEntity deployServiceEntity) {
        boolean isOwner = userServiceHelper.currentUserIsOwner(deployServiceEntity.getUserId());
        boolean isAdmin = userServiceHelper.currentUserHasRole(ROLE_ADMIN);
        return !isOwner && !isAdmin;
    }
}
