/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotSupportedForMonitoringException;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Monitor metrics service.
 */
@Slf4j
@Service
public class ServiceMetricsAdapter {

    private static final long FIVE_MINUTES_MILLISECONDS = 5 * 60 * 1000;
    private final DeployServiceStorage deployServiceStorage;
    private final DeployResourceStorage deployResourceStorage;
    private final PluginManager pluginManager;
    private final IdentityProviderManager identityProviderManager;

    /**
     * The constructor of Monitor.
     */
    public ServiceMetricsAdapter(DeployServiceStorage deployServiceStorage,
                                 DeployResourceStorage deployResourceStorage,
                                 PluginManager pluginManager,
                                 IdentityProviderManager identityProviderManager) {
        this.deployServiceStorage = deployServiceStorage;
        this.deployResourceStorage = deployResourceStorage;
        this.pluginManager = pluginManager;
        this.identityProviderManager = identityProviderManager;
    }

    /**
     * Get metrics of the service instance.
     */
    public List<Metric> getMetricsByServiceId(String id,
                                              MonitorResourceType monitorType,
                                              Long from,
                                              Long to,
                                              Integer granularity,
                                              boolean onlyLastKnownMetric) {
        validateToAndFromValues(from, to);
        DeployServiceEntity serviceEntity = findDeployServiceEntity(UUID.fromString(id));

        List<DeployResourceEntity> deployResourceList = serviceEntity.getDeployResourceList();
        List<DeployResource> deployResources =
                EntityTransUtils.transResourceEntity(deployResourceList);
        List<DeployResource> vmResources = deployResources.stream()
                .filter(deployResource -> DeployResourceKind.VM.equals(deployResource.getKind()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(vmResources)) {
            throw new ResourceNotFoundException("No resource found in the service.");
        }

        if (!StringUtils.equals(getCurrentLoginUserId(), serviceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view metrics of services belonging to other users.");
        }

        OrchestratorPlugin orchestratorPlugin =
                pluginManager.getOrchestratorPlugin(serviceEntity.getCsp());

        ServiceMetricsRequest serviceMetricRequest =
                getServiceMetricRequest(vmResources, monitorType, from,
                        to, granularity, onlyLastKnownMetric, serviceEntity.getUserId());

        return orchestratorPlugin.getMetricsForService(serviceMetricRequest);
    }


    /**
     * Get metrics of the resource instance.
     */
    public List<Metric> getMetricsByResourceId(String id,
                                               MonitorResourceType monitorType,
                                               Long from,
                                               Long to,
                                               Integer granularity,
                                               boolean onlyLastKnownMetric) {
        validateToAndFromValues(from, to);
        DeployResourceEntity resourceEntity =
                deployResourceStorage.findDeployResourceByResourceId(id);
        if (Objects.isNull(resourceEntity)) {
            throw new ResourceNotFoundException("Resource not found.");
        }
        if (!DeployResourceKind.VM.equals(resourceEntity.getKind())) {
            String errorMsg =
                    String.format("Resource kind %s not support.", resourceEntity.getKind());
            log.error(errorMsg);
            throw new ResourceNotSupportedForMonitoringException(errorMsg);
        }
        DeployResource deployResource = new DeployResource();
        BeanUtils.copyProperties(resourceEntity, deployResource);
        DeployServiceEntity serviceEntity = findDeployServiceEntity(
                resourceEntity.getDeployService().getId());

        if (!StringUtils.equals(getCurrentLoginUserId(), serviceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view metrics of services belonging to other users.");
        }

        OrchestratorPlugin orchestratorPlugin =
                pluginManager.getOrchestratorPlugin(serviceEntity.getCsp());
        ResourceMetricsRequest resourceMetricRequest =
                getResourceMetricRequest(deployResource, monitorType, from,
                        to, granularity, onlyLastKnownMetric, serviceEntity.getUserId());
        return orchestratorPlugin.getMetricsForResource(resourceMetricRequest);
    }


    private DeployServiceEntity findDeployServiceEntity(UUID id) {
        DeployServiceEntity serviceEntity =
                deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(serviceEntity)) {
            throw new ServiceNotDeployedException("Service not found.");
        }
        return serviceEntity;
    }

    private ResourceMetricsRequest getResourceMetricRequest(DeployResource deployResource,
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

        return new ResourceMetricsRequest(deployResource, monitorType, from, to,
                granularity, onlyLastKnownMetric, userId);
    }

    private String getCurrentLoginUserId() {
        CurrentUserInfo currentUserInfo = identityProviderManager.getCurrentUserInfo();
        if (Objects.nonNull(currentUserInfo)
                && StringUtils.isNotBlank(currentUserInfo.getUserId())) {
            return currentUserInfo.getUserId();
        } else {
            return "defaultUserId";
        }
    }

    private ServiceMetricsRequest getServiceMetricRequest(List<DeployResource> deployResources,
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
        return new ServiceMetricsRequest(deployResources, monitorType, from, to,
                granularity, onlyLastKnownMetric, userId);
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
