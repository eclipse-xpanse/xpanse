/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils.FlexibleEngineMonitorClient;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils.FlexibleEngineMonitorConverter;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on FlexibleEngine cloud.
 */
@Slf4j
@Component
public class FlexibleEngineOrchestratorPlugin implements OrchestratorPlugin {


    @Override
    public DeployResourceHandler getResourceHandler() {
        return new FlexibleEngineTerraformResourceHandler();
    }

    @Override
    public Csp getCsp() {
        return Csp.FLEXIBLE_ENGINE;
    }

    @Override
    public List<CredentialType> getAvailableCredentialTypes() {
        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);
        return credentialTypes;
    }

    @Override
    public List<AbstractCredentialInfo> getCredentialDefinitions() {
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        CredentialDefinition accessKey = new CredentialDefinition(
                getCsp(), "AK_SK", "The access key and security key.",
                CredentialType.VARIABLES, credentialVariables);
        credentialVariables.add(
                new CredentialVariable("OS_ACCESS_KEY",
                        "The access key."));
        credentialVariables.add(
                new CredentialVariable("OS_SECRET_KEY",
                        "The security key."));
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(accessKey);
        return credentialInfos;
    }

    @Override
    public List<Metric> getMetrics(AbstractCredentialInfo credential,
            DeployResource deployResource, MonitorResourceType monitorResourceType) {
        FlexibleEngineMonitorClient monitorClient =
                getFlexibleEngineMonitorClient((CredentialDefinition) credential);
        List<String> urlList = FlexibleEngineMonitorConverter.buildMonitorMetricUrls(deployResource,
                monitorResourceType);
        List<Metric> metrics = new ArrayList<>();
        for (String url : urlList) {
            HttpRequestBase request =
                    monitorClient.buildRequest(url, FlexibleEngineMonitorConverter.getHeaders());
            CloseableHttpResponse response = monitorClient.send(request);

            if (response.getStatusLine().getStatusCode() != 400) {
                log.error("Get FlexibleEngine Monitor metric error.code:{},reponse:{}",
                        response.getStatusLine().getStatusCode(), response);
                return metrics;
            }
            Metric metric = FlexibleEngineMonitorConverter.convertResponseToMetric(deployResource,
                    (String) request.getParams().getParameter("region"), response.getEntity());
            metrics.add(metric);
        }
        return metrics;
    }

    private FlexibleEngineMonitorClient getFlexibleEngineMonitorClient(
            CredentialDefinition credentialDefinition) {
        String accessKey = null;
        String securityKey = null;
        if (CredentialType.VARIABLES.toValue().equals(credentialDefinition.getType().toValue())) {
            List<CredentialVariable> variables = credentialDefinition.getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (FlexibleEngineMonitorConstants.OS_ACCESS_KEY.equals(
                        credentialVariable.getName())) {
                    accessKey = credentialVariable.getValue();
                }
                if (FlexibleEngineMonitorConstants.OS_SECRET_KEY.equals(
                        credentialVariable.getName())) {
                    securityKey = credentialVariable.getValue();
                }
            }
        }
        return new FlexibleEngineMonitorClient(accessKey, securityKey);
    }


}

