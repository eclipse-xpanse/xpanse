/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.Resource;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicemigration.DatabaseServiceMigrationStorage;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.database.serviceorder.DatabaseServiceOrderStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicepolicy.DatabaseServicePolicyStorage;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateEntity;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.userpolicy.DatabaseUserPolicyStorage;
import org.eclipse.xpanse.modules.database.userpolicy.UserPolicyEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.springframework.stereotype.Component;

/**
 * Bean provide methods to get the csp info.
 */
@Slf4j
@Component
public class GetCspInfoFromRequest {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;
    @Resource
    private DatabaseServicePolicyStorage servicePolicyStorage;
    @Resource
    private DatabaseUserPolicyStorage userPolicyStorage;
    @Resource
    private TaskService taskService;
    @Resource
    private DatabaseServiceMigrationStorage serviceMigrationStorage;
    @Resource
    private DatabaseServiceOrderStorage serviceOrderStorage;
    @Resource
    private DatabaseServiceOrderStorage serviceOrderTaskStorage;
    @Resource
    private ServiceRecreateStorage serviceRecreateStorage;

    /**
     * Get Csp with the URL of Ocl.
     *
     * @param url url string of Ocl.
     * @return csp.
     */
    public Csp getCspFromOclLocation(String url) {
        try {
            URL urlObj = URI.create(url).toURL();
            Ocl ocl = yamlMapper.readValue(urlObj, Ocl.class);
            return ocl.getCloudServiceProvider().getName();
        } catch (Exception e) {
            log.error("Get Csp of Ocl with url:{} failed.", url, e);
        }
        return null;
    }


    /**
     * Get Csp with id of service template.
     *
     * @param id id of service template.
     * @return csp.
     */
    public Csp getCspFromServiceTemplateId(String id) {
        try {
            ServiceTemplateEntity serviceTemplate =
                    serviceTemplateStorage.getServiceTemplateById(UUID.fromString(id));
            if (Objects.nonNull(serviceTemplate)) {
                return serviceTemplate.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service template id:{} failed.", id, e);
        }
        return null;
    }

    /**
     * Get Csp with id of service.
     *
     * @param id id of service.
     * @return csp.
     */
    public Csp getCspFromServiceId(String id) {
        try {
            DeployServiceEntity deployService =
                    deployServiceStorage.findDeployServiceById(UUID.fromString(id));
            if (Objects.nonNull(deployService)) {
                return deployService.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service id:{} failed.", id, e);
        }
        return null;
    }

    /**
     * Get Csp with id of migrate.
     *
     * @param id id of service.
     * @return csp.
     */
    public Csp getCspFromServiceMigrationId(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        try {
            ServiceMigrationEntity serviceMigrationEntity =
                    serviceMigrationStorage.findServiceMigrationById(UUID.fromString(id));
            if (Objects.nonNull(serviceMigrationEntity)) {
                DeployServiceEntity deployService =
                        deployServiceStorage.findDeployServiceById(
                                serviceMigrationEntity.getOldServiceId());
                return deployService.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service migration id:{} failed.", id, e);
        }
        return null;
    }

    /**
     * Get Csp with id of recreate.
     *
     * @param id id of service.
     * @return csp.
     */
    public Csp getCspFromServiceRecreateId(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        try {
            ServiceRecreateEntity serviceRecreateEntity =
                    serviceRecreateStorage.findServiceRecreateById(UUID.fromString(id));
            if (Objects.nonNull(serviceRecreateEntity)) {
                DeployServiceEntity deployService =
                        deployServiceStorage.findDeployServiceById(
                                serviceRecreateEntity.getServiceId());
                return deployService.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service migration id:{} failed.", id, e);
        }
        return null;
    }

    /**
     * Get Csp with id of user policy.
     *
     * @param id id of user policy.
     * @return csp.
     */
    public Csp getCspFromUserPolicyId(String id) {
        try {
            UserPolicyEntity userPolicy =
                    userPolicyStorage.findPolicyById(UUID.fromString(id));
            if (Objects.nonNull(userPolicy)) {
                return userPolicy.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with user policy id:{} failed.", id, e);
        }
        return null;
    }

    /**
     * Get Csp with id of user policy.
     *
     * @param id id of user policy.
     * @return csp.
     */
    public Csp getCspFromServicePolicyId(String id) {
        try {
            ServicePolicyEntity servicePolicy =
                    servicePolicyStorage.findPolicyById(UUID.fromString(id));
            if (Objects.nonNull(servicePolicy)) {
                return servicePolicy.getServiceTemplate().getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service policy id:{} failed.", id, e);
        }
        return null;
    }

    /**
     * Get Csp with taskId of migrate task.
     *
     * @param taskId id of migrate task.
     * @return csp.
     */
    public Csp getCsFromWorkflowTaskId(String taskId) {
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (Objects.nonNull(task)) {
                String processInstanceId = task.getProcessInstanceId();
                if (StringUtils.isNotBlank(processInstanceId)) {
                    ServiceMigrationEntity serviceMigrationEntity =
                            serviceMigrationStorage.findServiceMigrationById(
                                    UUID.fromString(processInstanceId));
                    if (Objects.nonNull(serviceMigrationEntity)) {
                        DeployServiceEntity deployService =
                                deployServiceStorage.findDeployServiceById(
                                        serviceMigrationEntity.getOldServiceId());
                        return deployService.getCsp();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Get csp with workflow task id:{} failed.", taskId, e);
        }
        return null;
    }

    /**
     * Get Csp with id of service state management task.
     *
     * @param managementTaskId id of service state management task.
     * @return csp.
     */
    public Csp getCspFromManagementTaskId(String managementTaskId) {
        try {
            ServiceOrderEntity task =
                    serviceOrderStorage.getEntityById(UUID.fromString(managementTaskId));
            if (Objects.nonNull(task) && Objects.nonNull(task.getServiceId())) {
                DeployServiceEntity deployService =
                        deployServiceStorage.findDeployServiceById(task.getServiceId());
                return deployService.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service state management task id:{} failed.",
                    managementTaskId, e);
        }
        return null;
    }


    /**
     * Get Csp with id of the service order.
     *
     * @param orderId id of the service order.
     * @return csp.
     */
    public Csp getCspFromServiceOrderId(String orderId) {
        try {
            ServiceOrderEntity order =
                    serviceOrderTaskStorage.getEntityById(UUID.fromString(orderId));
            if (Objects.nonNull(order) && Objects.nonNull(order.getServiceId())) {
                DeployServiceEntity deployService =
                        deployServiceStorage.findDeployServiceById(order.getServiceId());
                return deployService.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service order id:{} failed.", orderId, e);
        }
        return null;
    }
}
