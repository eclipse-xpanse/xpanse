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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryEntity;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryStorage;
import org.eclipse.xpanse.modules.database.userpolicy.UserPolicyEntity;
import org.eclipse.xpanse.modules.database.userpolicy.UserPolicyStorage;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean provide methods to get the csp info. */
@Slf4j
@Component
public class GetCspInfoFromRequest {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    @Resource private ServiceTemplateStorage serviceTemplateStorage;
    @Resource private ServiceDeploymentStorage deployServiceStorage;
    @Resource private ServicePolicyStorage servicePolicyStorage;
    @Resource private UserPolicyStorage userPolicyStorage;
    @Resource private TaskService taskService;
    @Resource private ServiceOrderStorage serviceOrderStorage;
    @Resource private ServiceTemplateRequestHistoryStorage serviceTemplateHistoryStorage;
    @Resource private ServiceChangeRequestStorage serviceChangeRequestStorage;

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
     * @param serviceTemplateId id of service template.
     * @return csp.
     */
    public Csp getCspFromServiceTemplateId(UUID serviceTemplateId) {
        try {
            ServiceTemplateEntity serviceTemplate =
                    serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
            if (Objects.nonNull(serviceTemplate)) {
                return serviceTemplate.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service template id:{} failed.", serviceTemplateId, e);
        }
        return null;
    }

    /**
     * Get Csp with id of service.
     *
     * @param serviceId id of service.
     * @return csp.
     */
    public Csp getCspFromServiceId(UUID serviceId) {
        try {
            ServiceDeploymentEntity deployService =
                    deployServiceStorage.findServiceDeploymentById(serviceId);
            if (Objects.nonNull(deployService)) {
                return deployService.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service id:{} failed.", serviceId, e);
        }
        return null;
    }

    /**
     * Get Csp with id of user policy.
     *
     * @param userPolicyId id of user policy.
     * @return csp.
     */
    public Csp getCspFromUserPolicyId(UUID userPolicyId) {
        try {
            UserPolicyEntity userPolicy = userPolicyStorage.findUserPolicyById(userPolicyId);
            if (Objects.nonNull(userPolicy)) {
                return userPolicy.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with user policy id:{} failed.", userPolicyId, e);
        }
        return null;
    }

    /**
     * Get Csp with id of service policy.
     *
     * @param servicePolicyId id of service policy.
     * @return csp.
     */
    public Csp getCspFromServicePolicyId(UUID servicePolicyId) {
        try {
            ServicePolicyEntity servicePolicy =
                    servicePolicyStorage.findPolicyById(servicePolicyId);
            if (Objects.nonNull(servicePolicy)) {
                return servicePolicy.getServiceTemplate().getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service policy id:{} failed.", servicePolicyId, e);
        }
        return null;
    }

    /**
     * Get Csp with taskId of porting task.
     *
     * @param taskId id of porting task.
     * @return csp.
     */
    public Csp getCspFromWorkflowTaskId(String taskId) {
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (Objects.nonNull(task)) {
                String processInstanceId = task.getProcessInstanceId();
                if (StringUtils.isNotBlank(processInstanceId)) {
                    ServiceOrderEntity queryOrderEntity = new ServiceOrderEntity();
                    queryOrderEntity.setWorkflowId(processInstanceId);
                    List<ServiceOrderEntity> orderEntities =
                            serviceOrderStorage.queryEntities(queryOrderEntity);
                    if (!CollectionUtils.isEmpty(orderEntities)) {
                        UUID serviceId = orderEntities.getFirst().getOriginalServiceId();
                        ServiceDeploymentEntity deployService =
                                deployServiceStorage.findServiceDeploymentById(serviceId);
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
     * Get Csp with id of the service order.
     *
     * @param orderId id of the service order.
     * @return csp.
     */
    public Csp getCspFromServiceOrderId(UUID orderId) {
        try {
            ServiceOrderEntity order = serviceOrderStorage.getEntityById(orderId);
            if (Objects.nonNull(order)
                    && Objects.nonNull(order.getServiceDeploymentEntity().getId())) {
                ServiceDeploymentEntity deployService =
                        deployServiceStorage.findServiceDeploymentById(
                                order.getServiceDeploymentEntity().getId());
                return deployService.getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service order id:{} failed.", orderId, e);
        }
        return null;
    }

    /**
     * Get Csp with service template request id.
     *
     * @param requestId id of service template request.
     * @return csp.
     */
    public Csp getCspFromServiceTemplateRequestId(UUID requestId) {
        try {
            ServiceTemplateRequestHistoryEntity serviceTemplateHistory =
                    serviceTemplateHistoryStorage.getEntityByRequestId(requestId);
            if (Objects.nonNull(serviceTemplateHistory)
                    && Objects.nonNull(serviceTemplateHistory.getServiceTemplate())) {
                return serviceTemplateHistory.getServiceTemplate().getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service template request id:{} failed.", requestId, e);
        }
        return null;
    }

    /**
     * Get Csp with service change request id.
     *
     * @param changeId id of service change request
     * @return csp.
     */
    public Csp getCspFromServiceChangeRequestId(UUID changeId) {
        try {
            ServiceChangeRequestEntity serviceChangeRequestEntity =
                    serviceChangeRequestStorage.findById(changeId);
            if (Objects.nonNull(serviceChangeRequestEntity)
                    && Objects.nonNull(serviceChangeRequestEntity.getServiceDeploymentEntity())) {
                return serviceChangeRequestEntity.getServiceDeploymentEntity().getCsp();
            }
        } catch (Exception e) {
            log.error("Get csp with service change request id:{} failed.", changeId, e);
        }
        return null;
    }
}
