/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudResourceManager;
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudVmStateManager;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.HuaweiCloudMetricsService;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.plugins.huaweicloud.resourcehandler.HuaweiCloudTerraformResourceHandler;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on Huawei cloud.
 */
@Slf4j
@Component
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin {

    @Resource
    private HuaweiCloudTerraformResourceHandler huaweiCloudTerraformResourceHandler;

    @Resource
    private HuaweiCloudMetricsService huaweiCloudMetricsService;

    @Resource
    private HuaweiCloudVmStateManager huaweiCloudVmStateManager;

    @Resource
    private HuaweiCloudResourceManager huaweiCloudResourceManager;

    @Override
    public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
        Map<DeployerKind, DeployResourceHandler> resourceHandlers = new HashMap<>();
        resourceHandlers.put(DeployerKind.TERRAFORM, huaweiCloudTerraformResourceHandler);
        resourceHandlers.put(DeployerKind.OPEN_TOFU, huaweiCloudTerraformResourceHandler);
        return resourceHandlers;
    }

    @Override
    public List<String> getExistingResourcesOfType(String userId, String region,
                                                   DeployResourceKind kind) {
        return huaweiCloudResourceManager.getExistingResourcesOfType(userId, region, kind);
    }

    @Override
    public Csp getCsp() {
        return Csp.HUAWEI;
    }

    @Override
    public List<String> requiredProperties() {
        return Collections.emptyList();
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
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY, "The access key.",
                        true));
        credentialVariables.add(new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                "The security key.", true));
        CredentialVariables accessKey = new CredentialVariables(getCsp(), CredentialType.VARIABLES,
                HuaweiCloudMonitorConstants.IAM,
                "Using The access key and security key authentication.", null, credentialVariables);

        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(accessKey);
        /* In the credential definition object CredentialVariables. The value of fields joined like
           csp-type-name must be unique. It means when you want to add a new CredentialVariables
           with type VARIABLES for this csp, the value of filed name in the new CredentialVariables
           must be different from the value of filed name in others CredentialVariables
           with the same type VARIABLES. Otherwise, it will throw an exception at the application
           startup.
           */
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
        return huaweiCloudMetricsService.getMetricsByResource(resourceMetricRequest);
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        return huaweiCloudMetricsService.getMetricsByService(serviceMetricRequest);
    }

    @Override
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        return huaweiCloudVmStateManager.startService(serviceStateManageRequest);
    }

    @Override
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        return huaweiCloudVmStateManager.stopService(serviceStateManageRequest);
    }

    @Override
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        return huaweiCloudVmStateManager.restartService(serviceStateManageRequest);
    }
}
