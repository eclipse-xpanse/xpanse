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
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.eclipse.xpanse.orchestrator.credential.CredentialCenter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Monitor.
 */
@Component
public class Monitor {

    private final CredentialCenter credentialCenter;
    private final DeployServiceStorage deployServiceStorage;
    private final OrchestratorService orchestratorService;

    /**
     * The constructor of Monitor.
     */
    public Monitor(CredentialCenter credentialCenter,
            DeployServiceStorage deployServiceStorage,
            OrchestratorService orchestratorService) {
        this.credentialCenter = credentialCenter;
        this.deployServiceStorage = deployServiceStorage;
        this.orchestratorService = orchestratorService;
    }

    /**
     * Get metrics of the service instance.
     */
    public List<Metric> getMetrics(UUID id, MonitorResourceType monitorResourceType) {
        DeployServiceEntity serviceEntity = deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(serviceEntity)) {
            throw new EntityNotFoundException("Service not found.");
        }
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                serviceEntity.getCsp(), serviceEntity.getUserName());

        OrchestratorPlugin orchestratorPlugin =
                orchestratorService.getOrchestratorPlugin(serviceEntity.getCsp());

        List<DeployResourceEntity> deployResourceList = serviceEntity.getDeployResourceList();
        List<Metric> metrics = new ArrayList<>();
        for (DeployResourceEntity deployResourceEntity : deployResourceList) {
            if (DeployResourceKind.VM.equals(deployResourceEntity.getKind())) {
                DeployResource deployResource = new DeployResource();
                BeanUtils.copyProperties(deployResourceEntity, deployResource);
                List<Metric> metricList = orchestratorPlugin.getMetrics(credential, deployResource,
                        monitorResourceType);
                metrics.addAll(metricList);
            }
        }
        return metrics;
    }

}
