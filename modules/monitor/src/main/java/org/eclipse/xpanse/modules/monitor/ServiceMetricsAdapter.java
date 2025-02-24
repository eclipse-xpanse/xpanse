/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceStorage;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotSupportedForMonitoringException;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** Monitor metrics service. */
@Slf4j
@Service
public class ServiceMetricsAdapter {

    private static final long FIVE_MINUTES_MILLISECONDS = 5 * 60 * 1000;
    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource private ServiceResourceStorage serviceResourceStorage;
    @Resource private PluginManager pluginManager;
    @Resource private UserServiceHelper userServiceHelper;

    /** Get metrics of the service instance. */
    public List<Metric> getMetricsByServiceId(
            UUID serviceId,
            MonitorResourceType monitorType,
            Long from,
            Long to,
            Integer granularity,
            boolean onlyLastKnownMetric) {
        validateToAndFromValues(from, to);
        ServiceDeploymentEntity serviceEntity = findDeployServiceEntity(serviceId);
        List<ServiceResourceEntity> vmEntities =
                serviceEntity.getDeployResources().stream()
                        .filter(entity -> entity.getResourceKind().equals(DeployResourceKind.VM))
                        .toList();
        if (CollectionUtils.isEmpty(vmEntities)) {
            throw new ResourceNotFoundException("No resource found in the service.");
        }

        checkPermission(serviceEntity);

        OrchestratorPlugin orchestratorPlugin =
                pluginManager.getOrchestratorPlugin(serviceEntity.getCsp());
        Region region = serviceEntity.getRegion();
        List<DeployResource> vmResources =
                EntityTranslationUtils.transToDeployResources(vmEntities);
        ServiceMetricsRequest serviceMetricRequest =
                getServiceMetricRequest(
                        serviceId,
                        region,
                        vmResources,
                        monitorType,
                        from,
                        to,
                        granularity,
                        onlyLastKnownMetric,
                        serviceEntity.getUserId());
        return orchestratorPlugin.getMetricsForService(serviceMetricRequest);
    }

    /** Get metrics of the resource instance. */
    public List<Metric> getMetricsByResourceId(
            UUID resourceId,
            MonitorResourceType monitorType,
            Long from,
            Long to,
            Integer granularity,
            boolean onlyLastKnownMetric) {
        validateToAndFromValues(from, to);
        ServiceResourceEntity resourceEntity =
                serviceResourceStorage.findServiceResourceByResourceId(String.valueOf(resourceId));
        if (Objects.isNull(resourceEntity)) {
            throw new ResourceNotFoundException("Resource not found.");
        }
        if (!DeployResourceKind.VM.equals(resourceEntity.getResourceKind())) {
            String errorMsg =
                    String.format(
                            "Resource kind %s not support.", resourceEntity.getResourceKind());
            log.error(errorMsg);
            throw new ResourceNotSupportedForMonitoringException(errorMsg);
        }
        DeployResource deployResource = new DeployResource();
        BeanUtils.copyProperties(resourceEntity, deployResource);
        ServiceDeploymentEntity serviceEntity =
                findDeployServiceEntity(resourceEntity.getServiceDeploymentEntity().getId());

        OrchestratorPlugin orchestratorPlugin =
                pluginManager.getOrchestratorPlugin(serviceEntity.getCsp());
        Region region = serviceEntity.getRegion();
        ResourceMetricsRequest resourceMetricRequest =
                getResourceMetricRequest(
                        resourceEntity.getServiceDeploymentEntity().getId(),
                        region,
                        deployResource,
                        monitorType,
                        from,
                        to,
                        granularity,
                        onlyLastKnownMetric,
                        serviceEntity.getUserId());
        return orchestratorPlugin.getMetricsForResource(resourceMetricRequest);
    }

    private ServiceDeploymentEntity findDeployServiceEntity(UUID id) {
        ServiceDeploymentEntity serviceEntity =
                serviceDeploymentStorage.findServiceDeploymentById(id);
        if (Objects.isNull(serviceEntity)) {
            throw new ServiceNotDeployedException("Service not found.");
        }
        checkPermission(serviceEntity);
        return serviceEntity;
    }

    private void checkPermission(ServiceDeploymentEntity serviceEntity) {
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(serviceEntity.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.",
                            UserOperation.VIEW_METRICS_OF_SERVICE.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
    }

    private ResourceMetricsRequest getResourceMetricRequest(
            UUID serviceId,
            Region region,
            DeployResource deployResource,
            MonitorResourceType monitorType,
            Long from,
            Long to,
            Integer granularity,
            boolean onlyLastKnownMetric,
            String userId) {
        if (onlyLastKnownMetric) {
            from = null;
            to = null;
        } else {
            if (Objects.isNull(from)) {
                from = System.currentTimeMillis() - FIVE_MINUTES_MILLISECONDS;
            }
            if (Objects.isNull(to)) {
                to = System.currentTimeMillis();
            }
        }

        return new ResourceMetricsRequest(
                serviceId,
                region,
                deployResource,
                monitorType,
                from,
                to,
                granularity,
                onlyLastKnownMetric,
                userId);
    }

    private ServiceMetricsRequest getServiceMetricRequest(
            UUID serviceId,
            Region region,
            List<DeployResource> deployResources,
            MonitorResourceType monitorType,
            Long from,
            Long to,
            Integer granularity,
            boolean onlyLastKnownMetric,
            String userId) {
        if (onlyLastKnownMetric) {
            from = null;
            to = null;
        } else {
            if (Objects.isNull(from)) {
                from = System.currentTimeMillis() - FIVE_MINUTES_MILLISECONDS;
            }
            if (Objects.isNull(to)) {
                to = System.currentTimeMillis();
            }
        }
        return new ServiceMetricsRequest(
                serviceId,
                region,
                deployResources,
                monitorType,
                from,
                to,
                granularity,
                onlyLastKnownMetric,
                userId);
    }

    private void validateToAndFromValues(Long from, Long to) {
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            if (from >= to) {
                throw new IllegalArgumentException(
                        "The value of parameter 'from' must be less than "
                                + "the value of parameter 'to'.");
            }
            if (from > System.currentTimeMillis()) {
                throw new IllegalArgumentException(
                        "The value of parameter 'from' must be less than the UNIX timestamp "
                                + "in milliseconds of the current time.");
            }
        }
    }
}
