/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.audit.AuditLog;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.openstack.common.constants.OpenstackEnvironmentConstants;
import org.eclipse.xpanse.plugins.openstack.manage.OpenstackResourceManager;
import org.eclipse.xpanse.plugins.openstack.manage.OpenstackServersManager;
import org.eclipse.xpanse.plugins.openstack.monitor.MetricsManager;
import org.eclipse.xpanse.plugins.openstack.price.OpenstackPriceCalculator;
import org.eclipse.xpanse.plugins.openstack.resourcehandler.OpenstackTerraformResourceHandler;
import org.springframework.stereotype.Component;

/**
 * xpanse plugin implementation for openstack cloud.
 */
@Slf4j
@Component
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin {
    @Resource
    private OpenstackTerraformResourceHandler openstackTerraformResourceHandler;
    @Resource
    private MetricsManager metricsManager;
    @Resource
    private OpenstackServersManager openstackServersManager;
    @Resource
    private OpenstackResourceManager openStackResourceManager;
    @Resource
    private OpenstackPriceCalculator openstackPriceCalculator;

    /**
     * Get the resource handlers for OpenStack.
     */
    @Override
    public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
        Map<DeployerKind, DeployResourceHandler> resourceHandlers = new HashMap<>();
        resourceHandlers.put(DeployerKind.TERRAFORM, openstackTerraformResourceHandler);
        resourceHandlers.put(DeployerKind.OPEN_TOFU, openstackTerraformResourceHandler);
        return resourceHandlers;
    }

    @Override
    public List<String> getExistingResourceNamesWithKind(String userId, String region,
                                                         DeployResourceKind kind) {
        return openStackResourceManager.getExistingResourceNamesWithKind(userId, region, kind);
    }

    @Override
    public List<String> getAvailabilityZonesOfRegion(String userId, String region) {
        return openStackResourceManager.getAvailabilityZonesOfRegion(userId, region);
    }

    /**
     * Get the cloud service provider.
     */
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
        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);
        return credentialTypes;
    }

    @Override
    public List<AbstractCredentialInfo> getCredentialDefinitions() {
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.PROJECT,
                "The Name of the Tenant or Project to use.", true, false));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.USERNAME,
                "The Username to login with.", true, false));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.PASSWORD,
                "The Password to login with.", true, true));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.USER_DOMAIN,
                "The domain to which the openstack user is linked to.", true, false));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.PROJECT_DOMAIN,
                "The domain to which the openstack project is linked to.", true, false));
        CredentialVariables httpAuth =
                new CredentialVariables(getCsp(), CredentialType.VARIABLES, "USERNAME_PASSWORD",
                        "Authenticate at the specified URL using an account and password.", null,
                        credentialVariables);
        List<AbstractCredentialInfo> credentialInfos = new ArrayList<>();
        credentialInfos.add(httpAuth);

        /* In the credential definition, object CredentialVariables. The value of fields joined like
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
     * @return Returns list of metric results.
     */
    @Override
    public List<Metric> getMetricsForResource(ResourceMetricsRequest resourceMetricRequest) {
        return this.metricsManager.getMetrics(resourceMetricRequest);
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        List<Metric> metrics = new ArrayList<>();
        for (DeployResource deployResource : serviceMetricRequest.getDeployResources()) {
            ResourceMetricsRequest resourceMetricRequest =
                    new ResourceMetricsRequest(serviceMetricRequest.getServiceId(), deployResource,
                            serviceMetricRequest.getMonitorResourceType(),
                            serviceMetricRequest.getFrom(), serviceMetricRequest.getTo(),
                            serviceMetricRequest.getGranularity(),
                            serviceMetricRequest.isOnlyLastKnownMetric(),
                            serviceMetricRequest.getUserId());
            metrics.addAll(this.metricsManager.getMetrics(resourceMetricRequest));
        }
        return metrics;
    }

    @Override
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        return openstackServersManager.startService(serviceStateManageRequest);
    }

    @Override
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        return openstackServersManager.stopService(serviceStateManageRequest);
    }

    @Override
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        return openstackServersManager.restartService(serviceStateManageRequest);
    }

    @Override
    public void auditApiRequest(AuditLog auditLog) {
        log.info(auditLog.toString());
    }

    @Override
    public FlavorPriceResult getServicePrice(ServicePriceRequest request) {
        return openstackPriceCalculator.getServicePrice(request);
    }
}
