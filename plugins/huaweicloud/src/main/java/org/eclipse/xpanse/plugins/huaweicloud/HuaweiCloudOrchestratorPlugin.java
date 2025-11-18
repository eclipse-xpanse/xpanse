/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.REGION_AZS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.SERVICE_FLAVOR_PRICE_CACHE_NAME;

import com.huaweicloud.sdk.iam.v3.region.IamRegion;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateReviewPluginResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.UnavailableServiceRegionsException;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.audit.AuditLog;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudConstants;
import org.eclipse.xpanse.plugins.huaweicloud.config.HuaweiCloudPluginProperties;
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudResourceManager;
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudVmStateManager;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.HuaweiCloudMetricsService;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.plugins.huaweicloud.price.HuaweiCloudPriceCalculator;
import org.eclipse.xpanse.plugins.huaweicloud.resourcehandler.HuaweiCloudTerraformResourceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Plugin to deploy managed services on Huawei cloud. */
@Slf4j
@Component
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin {

    private final HuaweiCloudTerraformResourceHandler terraformResourceHandler;
    private final HuaweiCloudMetricsService metricsService;
    private final HuaweiCloudVmStateManager vmStateManager;
    private final HuaweiCloudResourceManager resourceManager;
    private final HuaweiCloudPriceCalculator priceCalculator;
    private final HuaweiCloudPluginProperties huaweiCloudPluginProperties;

    /** Constructor method. */
    @Autowired
    public HuaweiCloudOrchestratorPlugin(
            HuaweiCloudTerraformResourceHandler terraformResourceHandler,
            HuaweiCloudMetricsService metricsService,
            HuaweiCloudVmStateManager vmStateManager,
            HuaweiCloudResourceManager resourceManager,
            HuaweiCloudPriceCalculator priceCalculator,
            HuaweiCloudPluginProperties huaweiCloudPluginProperties) {
        this.terraformResourceHandler = terraformResourceHandler;
        this.metricsService = metricsService;
        this.vmStateManager = vmStateManager;
        this.resourceManager = resourceManager;
        this.priceCalculator = priceCalculator;
        this.huaweiCloudPluginProperties = huaweiCloudPluginProperties;
    }

    @Override
    public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
        Map<DeployerKind, DeployResourceHandler> resourceHandlers = new HashMap<>();
        resourceHandlers.put(DeployerKind.TERRAFORM, terraformResourceHandler);
        resourceHandlers.put(DeployerKind.OPEN_TOFU, terraformResourceHandler);
        return resourceHandlers;
    }

    @Override
    public List<String> getExistingResourceNamesWithKind(
            String site, String region, String userId, DeployResourceKind kind, UUID serviceId) {
        return resourceManager.getExistingResourceNamesWithKind(site, region, userId, kind);
    }

    @Override
    @Cacheable(cacheNames = REGION_AZS_CACHE_NAME)
    public List<String> getAvailabilityZonesOfRegion(
            String siteName,
            String regionName,
            String userId,
            UUID serviceId,
            UUID serviceTemplateId) {
        return resourceManager.getAvailabilityZonesOfRegion(siteName, regionName, userId);
    }

    @Override
    public Csp getCsp() {
        return Csp.HUAWEI_CLOUD;
    }

    @Override
    public List<String> requiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getEnvVarKeysMappingMap() {
        return Collections.emptyMap();
    }

    @Override
    public ServiceTemplateReviewPluginResultType validateServiceTemplate(Ocl ocl) {
        if (huaweiCloudPluginProperties.getServiceTemplate().getAutoApprove()) {
            return ServiceTemplateReviewPluginResultType.APPROVED;
        }
        return ServiceTemplateReviewPluginResultType.MANUAL_REVIEW_REQUIRED;
    }

    @Override
    public void prepareServiceTemplate(Ocl ocl) {
        log.info("prepare service template.");
    }

    @Override
    public List<String> getSites() {
        return List.of(
                HuaweiCloudConstants.INTERNATIONAL_SITE,
                HuaweiCloudConstants.CHINESE_MAINLAND_SITE,
                HuaweiCloudConstants.EUROPE_SITE);
    }

    @Override
    public boolean validateRegionsOfService(Ocl ocl) {
        List<String> errors = new ArrayList<>();
        List<Region> regions = ocl.getCloudServiceProvider().getRegions();
        regions.forEach(
                region -> {
                    try {
                        IamRegion.valueOf(region.getName());
                    } catch (IllegalArgumentException e) {
                        String errorMsg =
                                String.format(
                                        "Region with name %s is unavailable in " + "Csp %s.",
                                        region.getName(), getCsp().toValue());
                        errors.add(errorMsg);
                    }
                    if (!getSites().contains(region.getSite())) {
                        String errorMsg =
                                String.format(
                                        "Region with site %s is unavailable in Csp %s. "
                                                + "Available sites %s",
                                        region.getName(), getCsp().toValue(), getSites());
                        errors.add(errorMsg);
                    }
                });
        if (CollectionUtils.isEmpty(errors)) {
            return true;
        }
        throw new UnavailableServiceRegionsException(errors);
    }

    @Override
    public Map<String, String> getComputeResourcesInServiceDeployment(File scriptFile) {
        return resourceManager.getComputeResourcesInServiceDeployment(scriptFile);
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
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_ACCESS_KEY, "The access key.", true));
        credentialVariables.add(
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_SECRET_KEY, "The security key.", true));
        CredentialVariables accessKey =
                new CredentialVariables(
                        getCsp(),
                        getSites().getFirst(),
                        CredentialType.VARIABLES,
                        HuaweiCloudMonitorConstants.IAM,
                        "Using The access key and security key authentication.",
                        null,
                        credentialVariables);

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
     * Get metrics for resource instance by the MetricRequest.
     *
     * @param resourceMetricRequest The request model to query metrics for resource instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForResource(ResourceMetricsRequest resourceMetricRequest) {
        return metricsService.getMetricsByResource(resourceMetricRequest);
    }

    /**
     * Get metrics for service instance by the @serviceMetricRequest.
     *
     * @param serviceMetricRequest The request model to query metrics for service instance.
     * @return Returns list of metric result.
     */
    @Override
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        return metricsService.getMetricsByService(serviceMetricRequest);
    }

    @Override
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        return vmStateManager.startService(serviceStateManageRequest);
    }

    @Override
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        return vmStateManager.stopService(serviceStateManageRequest);
    }

    @Override
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        return vmStateManager.restartService(serviceStateManageRequest);
    }

    @Override
    public void auditApiRequest(AuditLog auditLog) {
        log.info(auditLog.toString());
    }

    @Override
    @Cacheable(cacheNames = SERVICE_FLAVOR_PRICE_CACHE_NAME, key = "#request")
    public FlavorPriceResult getServiceFlavorPrice(ServiceFlavorPriceRequest request) {
        return priceCalculator.getServiceFlavorPrice(request);
    }
}
