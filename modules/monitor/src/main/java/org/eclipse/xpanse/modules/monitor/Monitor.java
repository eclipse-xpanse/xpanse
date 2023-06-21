/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Monitor.
 */
@Slf4j
@Component
public class Monitor {

    private static final long FIVE_MINUTES_MILLISECONDS = 5 * 60 * 1000;
    private final DeployServiceStorage deployServiceStorage;
    private final DeployResourceStorage deployResourceStorage;
    private final PluginManager pluginManager;

    /**
     * The constructor of Monitor.
     */
    public Monitor(DeployServiceStorage deployServiceStorage,
            DeployResourceStorage deployResourceStorage,
            PluginManager pluginManager) {
        this.deployServiceStorage = deployServiceStorage;
        this.deployResourceStorage = deployResourceStorage;
        this.pluginManager = pluginManager;
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
            throw new EntityNotFoundException("No resource found in the service.");
        }

        OrchestratorPlugin orchestratorPlugin =
                pluginManager.getOrchestratorPlugin(serviceEntity.getCsp());

        ServiceMetricRequest serviceMetricRequest =
                getServiceMetricRequest(vmResources, monitorType, from,
                        to, granularity, onlyLastKnownMetric, serviceEntity.getUserName());

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
            throw new EntityNotFoundException("Resource not found.");
        }

        if (!DeployResourceKind.VM.equals(resourceEntity.getKind())) {
            String errorMsg =
                    String.format("Resource kind %s not support.", resourceEntity.getKind());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        DeployResource deployResource = new DeployResource();
        BeanUtils.copyProperties(resourceEntity, deployResource);
        DeployServiceEntity serviceEntity = findDeployServiceEntity(
                resourceEntity.getDeployService().getId());
        OrchestratorPlugin orchestratorPlugin =
                pluginManager.getOrchestratorPlugin(serviceEntity.getCsp());
        ResourceMetricRequest resourceMetricRequest =
                getResourceMetricRequest(deployResource, monitorType, from,
                        to, granularity, onlyLastKnownMetric, serviceEntity.getUserName());
        return orchestratorPlugin.getMetricsForResource(resourceMetricRequest);
    }


    private DeployServiceEntity findDeployServiceEntity(UUID id) {
        DeployServiceEntity serviceEntity =
                deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(serviceEntity)) {
            throw new EntityNotFoundException("Service not found.");
        }
        return serviceEntity;
    }

    private ResourceMetricRequest getResourceMetricRequest(DeployResource deployResource,
                                                           MonitorResourceType monitorType,
                                                           Long from,
                                                           Long to,
                                                           Integer granularity,
                                                           boolean onlyLastKnownMetric,
                                                           String xpanseUserName) {
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

        return new ResourceMetricRequest(deployResource, monitorType, from, to,
                granularity, onlyLastKnownMetric, xpanseUserName);
    }

    private ServiceMetricRequest getServiceMetricRequest(List<DeployResource> deployResources,
                                                         MonitorResourceType monitorType,
                                                         Long from,
                                                         Long to,
                                                         Integer granularity,
                                                         boolean onlyLastKnownMetric,
                                                         String xpanseUserName) {
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
        return new ServiceMetricRequest(deployResources, monitorType, from, to,
                granularity, onlyLastKnownMetric, xpanseUserName);
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
