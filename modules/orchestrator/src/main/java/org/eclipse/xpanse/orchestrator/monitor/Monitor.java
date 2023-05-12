/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.monitor;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ServiceInstance;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.credential.CredentialCenter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Monitor.
 */
@Component
public class Monitor {

    private final CredentialCenter credentialCenter;

    public Monitor(CredentialCenter credentialCenter) {
        this.credentialCenter = credentialCenter;
    }

    /**
     * Get metrics of the service instance.
     */
    public List<Metric> getMetrics(OrchestratorPlugin plugin,
                                   ServiceInstance serviceInstance) {

        List<DeployResourceEntity> deployResourceList =
                serviceInstance.getServiceEntity().getDeployResourceList();
        AbstractCredentialInfo credential = credentialCenter.getCredential(
                serviceInstance.getServiceEntity().getCsp(),
                serviceInstance.getServiceEntity().getUserName());
        List<Metric> metrics = new ArrayList<>();
        for (DeployResourceEntity deployResourceEntity : deployResourceList) {
            if (DeployResourceKind.VM.equals(deployResourceEntity.getKind())) {
                DeployResource deployResource = new DeployResource();
                BeanUtils.copyProperties(deployResourceEntity, deployResource);
                List<Metric> metricList = plugin.getMetrics(credential, deployResource);
                metrics.addAll(metricList);
            }
        }
        return metrics;
    }

}
