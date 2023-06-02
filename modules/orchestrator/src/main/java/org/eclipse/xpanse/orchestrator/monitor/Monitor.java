/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.monitor;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.eclipse.xpanse.orchestrator.credential.CredentialCenter;
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
    private final CredentialCenter credentialCenter;
    private final DeployServiceStorage deployServiceStorage;
    private final DeployResourceStorage deployResourceStorage;
    private final OrchestratorService orchestratorService;

    /**
     * The constructor of Monitor.
     */
    public Monitor(CredentialCenter credentialCenter,
                   DeployServiceStorage deployServiceStorage,
                   DeployResourceStorage deployResourceStorage,
                   OrchestratorService orchestratorService) {
        this.credentialCenter = credentialCenter;
        this.deployServiceStorage = deployServiceStorage;
        this.deployResourceStorage = deployResourceStorage;
        this.orchestratorService = orchestratorService;
    }


    /**
     * Get metrics of the service instance.
     */
    public List<Metric> getMetrics(UUID id, MonitorResourceType monitorType, Long from, Long to,
                                   Integer granularity) {
        DeployServiceEntity serviceEntity = deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(serviceEntity)) {
            throw new EntityNotFoundException("Service not found.");
        }
        CredentialDefinition credential = credentialCenter.getCredential(
                serviceEntity.getCsp(), serviceEntity.getUserName());

        OrchestratorPlugin orchestratorPlugin =
                orchestratorService.getOrchestratorPlugin(serviceEntity.getCsp());

        List<DeployResource> deployResources =
                EntityTransUtils.transResourceEntity(serviceEntity.getDeployResourceList());
        List<Metric> metrics = new ArrayList<>();
        for (DeployResource deployResource : deployResources) {
            if (DeployResourceKind.VM.equals(deployResource.getKind())) {
                ResourceMetricRequest resourceMetricRequest =
                        getResourceMetricRequest(deployResource, credential, monitorType, from, to,
                                granularity);
                List<Metric> metricList = orchestratorPlugin.getMetrics(resourceMetricRequest);
                metrics.addAll(metricList);
            }
        }
        return metrics;
    }


    /**
     * Get metrics of the service instance.
     */
    public List<Metric> getMetricsByServiceId(String id, MonitorResourceType monitorType,
                                              Long from,
                                              Long to, Integer granularity) {
        validateToAndFromValues(from, to);
        DeployServiceEntity serviceEntity = findDeployServiceEntity(UUID.fromString(id));

        List<DeployResourceEntity> deployResourceList = serviceEntity.getDeployResourceList();
        List<DeployResource> deployResources =
                EntityTransUtils.transResourceEntity(deployResourceList);
        List<DeployResource> vmResources = deployResources.stream()
                .filter(deployResource -> DeployResourceKind.VM.equals(deployResource.getKind()))
                .toList();
        if (CollectionUtils.isEmpty(vmResources)) {
            throw new EntityNotFoundException("No resource found in the service.");
        }

        CredentialDefinition credential =
                getCredential(serviceEntity.getCsp(), serviceEntity.getUserName());

        OrchestratorPlugin orchestratorPlugin =
                orchestratorService.getOrchestratorPlugin(serviceEntity.getCsp());
        List<Metric> metrics = new ArrayList<>();
        for (DeployResource vmResource : vmResources) {
            ResourceMetricRequest resourceMetricRequest =
                    getResourceMetricRequest(vmResource, credential, monitorType, from, to,
                            granularity);
            List<Metric> metricList =
                    orchestratorPlugin.getMetricsForResource(resourceMetricRequest);
            metrics.addAll(metricList);
        }
        return metrics;
    }


    /**
     * Get metrics of the resource instance.
     */
    public List<Metric> getMetricsByResourceId(String id, MonitorResourceType monitorType,
                                               Long from,
                                               Long to, Integer granularity) {
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
        DeployServiceEntity serviceEntity = findDeployServiceEntity(resourceEntity.getId());
        CredentialDefinition credential =
                getCredential(serviceEntity.getCsp(), serviceEntity.getUserName());
        OrchestratorPlugin orchestratorPlugin =
                orchestratorService.getOrchestratorPlugin(serviceEntity.getCsp());
        ResourceMetricRequest resourceMetricRequest =
                getResourceMetricRequest(deployResource, credential, monitorType, from,
                        to, granularity);
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

    private CredentialDefinition getCredential(Csp csp, String userName) {
        CredentialDefinition credential = credentialCenter.getCredential(
                csp, userName);
        if (Objects.isNull(credential)) {
            throw new EntityNotFoundException("Credential not found.");
        }
        return credential;
    }


    private ResourceMetricRequest getResourceMetricRequest(DeployResource deployResource,
                                                           CredentialDefinition credential,
                                                           MonitorResourceType monitorType,
                                                           Long from, Long to,
                                                           Integer granularity) {
        if (Objects.isNull(from)) {
            from = System.currentTimeMillis() - FIVE_MINUTES_MILLISECONDS;
        }
        if (Objects.isNull(to)) {
            to = System.currentTimeMillis();
        }
        return new ResourceMetricRequest(deployResource, credential, monitorType, from, to,
                granularity);
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
