/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.audit.AuditLog;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.springframework.stereotype.Component;

/**
 * Dummy plugin implementation just for tests.
 */
@Log
@Component
public class DummyPluginImpl implements OrchestratorPlugin {

    @Override
    public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
        return new HashMap<>();
    }

    @Override
    public List<String> getExistingResourceNamesWithKind(String userId, String region,
                                                         DeployResourceKind kind) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getAvailabilityZonesOfRegion(String userId, String region,
                                                     UUID serviceId) {
        return new ArrayList<>();
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
    public boolean autoApproveServiceTemplateIsEnabled() {
        return false;
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
    public void auditApiRequest(AuditLog auditLog) {
        log.info(auditLog.toString());
    }

    @Override
    public FlavorPriceResult getServicePrice(ServicePriceRequest request) {
        return null;
    }
}

