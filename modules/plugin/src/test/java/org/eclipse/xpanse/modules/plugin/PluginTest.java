/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin;

import java.util.List;
import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.plugin.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.plugin.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.plugin.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.plugin.monitor.Metric;
import org.eclipse.xpanse.modules.plugin.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.plugin.monitor.ServiceMetricRequest;
import org.springframework.stereotype.Component;

@Log
@Component
public class PluginTest implements OrchestratorPlugin {

    @Override
    public DeployResourceHandler getResourceHandler() {
        return null;
    }

    @Override
    public Csp getCsp() {
        return Csp.OPENSTACK;
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
     * @param resourceMetricRequest
     */
    @Override
    public List<Metric> getMetrics(ResourceMetricRequest resourceMetricRequest) {
        return null;
    }

    /**
     * Get metrics for resource instance by the @resourceMetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForResource(ResourceMetricRequest resourceMetricRequest) {
        return null;
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricRequest serviceMetricRequest) {
        return null;
    }
}
