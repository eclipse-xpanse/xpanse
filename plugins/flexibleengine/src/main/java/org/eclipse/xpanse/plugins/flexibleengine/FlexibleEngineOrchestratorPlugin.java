/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.manage.ServiceManagerRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.flexibleengine.manage.util.FlexibleEngineVmStateManagerService;
import org.eclipse.xpanse.plugins.flexibleengine.models.constant.FlexibleEngineConstants;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Plugin to deploy managed services on FlexibleEngine cloud.
 */
@Slf4j
@Component
public class FlexibleEngineOrchestratorPlugin implements OrchestratorPlugin {

    private final FlexibleEngineTerraformResourceHandler flexibleEngineTerraformResourceHandler;
    private final FlexibleEngineMetricsService flexibleEngineMetricsService;
    private final FlexibleEngineVmStateManagerService flexibleEngineVmStateManagerService;

    @Value("${terraform.provider.flexibleengine.version}")
    private String terraformFlexibleEngineVersion;

    /**
     * Building OrchestratorPlugin for FlexibleEngine.
     */
    @Autowired
    public FlexibleEngineOrchestratorPlugin(
            FlexibleEngineMetricsService flexibleEngineMetricsService,
            FlexibleEngineTerraformResourceHandler flexibleEngineTerraformResourceHandler,
            FlexibleEngineVmStateManagerService flexibleEngineVmStateManagerService) {
        this.flexibleEngineMetricsService = flexibleEngineMetricsService;
        this.flexibleEngineTerraformResourceHandler = flexibleEngineTerraformResourceHandler;
        this.flexibleEngineVmStateManagerService = flexibleEngineVmStateManagerService;
    }

    @Override
    public DeployResourceHandler getResourceHandler() {
        return this.flexibleEngineTerraformResourceHandler;
    }

    @Override
    public Csp getCsp() {
        return Csp.FLEXIBLE_ENGINE;
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
        CredentialVariables accessKey = new CredentialVariables(
                getCsp(), CredentialType.VARIABLES, "AK_SK", "The access key and security key.",
                null, credentialVariables);
        credentialVariables.add(
                new CredentialVariable(FlexibleEngineConstants.OS_ACCESS_KEY,
                        "The access key.", true));
        credentialVariables.add(
                new CredentialVariable(FlexibleEngineConstants.OS_SECRET_KEY,
                        "The security key.", true));
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
        return flexibleEngineMetricsService.getMetricsForResource(resourceMetricRequest);
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        return flexibleEngineMetricsService.getMetricsForService(serviceMetricRequest);
    }

    @Override
    public boolean startService(ServiceManagerRequest serviceManagerRequest) {
        return flexibleEngineVmStateManagerService.startService(serviceManagerRequest);
    }

    @Override
    public boolean stopService(ServiceManagerRequest serviceManagerRequest) {
        return flexibleEngineVmStateManagerService.stopService(serviceManagerRequest);
    }

    @Override
    public boolean restartService(ServiceManagerRequest serviceManagerRequest) {
        return flexibleEngineVmStateManagerService.restartService(serviceManagerRequest);
    }

    @Override
    public String getProvider(String region) {
        return String.format("""         
            terraform {
              required_providers {
                flexibleengine = {
                  source  = "FlexibleEngineCloud/flexibleengine"
                  version = "%s"
                }
              }
            }
                        
            provider "flexibleengine" {
              region = "%s"
            }
            """, terraformFlexibleEngineVersion, region);
    }
}
