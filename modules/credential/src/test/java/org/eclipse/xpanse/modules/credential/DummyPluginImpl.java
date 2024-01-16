/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.springframework.stereotype.Component;

/**
 * Dummy plugin implementation just for tests.
 */
@Log
@Component
public class DummyPluginImpl implements OrchestratorPlugin {

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
        return Collections.emptyList();
    }

    @Override
    public List<CredentialType> getAvailableCredentialTypes() {
        return null;
    }

    @Override
    public List<AbstractCredentialInfo> getCredentialDefinitions() {
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        CredentialVariables accessKey = new CredentialVariables(
                getCsp(), CredentialType.VARIABLES, "AK_SK", "The access key and security key.",
                null, credentialVariables);
        credentialVariables.add(
                new CredentialVariable("OS_ACCESS_KEY",
                        "The access key.", true));
        credentialVariables.add(
                new CredentialVariable("OS_SECRET_KEY",
                        "The security key.", true));
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(accessKey);
        return credentialInfos;
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
    public String getProvider(String region) {
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
}

