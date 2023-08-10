/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator;

import java.util.List;
import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.springframework.stereotype.Component;

@Log
@Component
public class DummyOpenstackPluginImpl implements OrchestratorPlugin {

    @Override
    public DeployResourceHandler getResourceHandler() {
        return null;
    }

    @Override
    public Csp getCsp() {
        return Csp.OPENSTACK;
    }

    @Override
    public List<String> requiredProperties() {
        return List.of("TEST_VAR");
    }

    @Override
    public List<CredentialType> getAvailableCredentialTypes() {
        return null;
    }

    @Override
    public List<AbstractCredentialInfo> getCredentialDefinitions() {
        return null;
    }

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForResource(ResourceMetricsRequest resourceMetricRequest) {
        return null;
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        return null;
    }
}
