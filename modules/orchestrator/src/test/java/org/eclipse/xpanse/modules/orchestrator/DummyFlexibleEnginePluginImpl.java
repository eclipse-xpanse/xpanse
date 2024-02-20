/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.springframework.stereotype.Component;

@Log
@Component
public class DummyFlexibleEnginePluginImpl implements OrchestratorPlugin {

    @Override
    public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
        return null;
    }

    @Override
    public List<String> getExistingResourcesOfType(String userId, String region,
            DeployResourceKind kind) {
        return new ArrayList<>();
    }

    @Override
    public Csp getCsp() {
        return Csp.FLEXIBLE_ENGINE;
    }

    @Override
    public List<String> requiredProperties() {
        return List.of("TEST_VAR_1", "TEST_VAR_2");
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



    @Override
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        return true;
    }

    @Override
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        return true;
    }

    @Override
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        return true;
    }

    @Override
    public String getProvider(DeployerKind deployerKind, String region) {
        return null;
    }
}
