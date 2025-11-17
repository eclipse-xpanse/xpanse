/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.REGION_AZS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.SERVICE_FLAVOR_PRICE_CACHE_NAME;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.DEFAULT_SITE;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.ENDPOINT_SUFFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.IAM_ENDPOINT_PREFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.PROTOCOL_HTTPS;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants;
import org.eclipse.xpanse.plugins.flexibleengine.config.FlexibleEnginePluginProperties;
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineResourceManager;
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineVmStateManager;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.FlexibleEngineMetricsService;
import org.eclipse.xpanse.plugins.flexibleengine.price.FlexibleEnginePriceCalculator;
import org.eclipse.xpanse.plugins.flexibleengine.resourcehandler.FlexibleEngineTerraformResourceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Plugin to deploy managed services on FlexibleEngine cloud. */
@Slf4j
@Component
public class FlexibleEngineOrchestratorPlugin implements OrchestratorPlugin {

    private final RestTemplate restTemplate = new RestTemplate();
    private final FlexibleEngineTerraformResourceHandler terraformResourceHandler;
    private final FlexibleEngineMetricsService metricsService;
    private final FlexibleEngineVmStateManager vmStateManager;
    private final FlexibleEngineResourceManager resourceManager;
    private final FlexibleEnginePriceCalculator pricingCalculator;
    private final FlexibleEnginePluginProperties flexibleEnginePluginProperties;

    /** Constructor method. */
    @Autowired
    public FlexibleEngineOrchestratorPlugin(
            FlexibleEngineTerraformResourceHandler terraformResourceHandler,
            FlexibleEngineMetricsService metricsService,
            FlexibleEngineVmStateManager vmStateManager,
            FlexibleEngineResourceManager resourceManager,
            FlexibleEnginePriceCalculator pricingCalculator,
            FlexibleEnginePluginProperties flexibleEnginePluginProperties) {
        this.terraformResourceHandler = terraformResourceHandler;
        this.metricsService = metricsService;
        this.vmStateManager = vmStateManager;
        this.resourceManager = resourceManager;
        this.pricingCalculator = pricingCalculator;
        this.flexibleEnginePluginProperties = flexibleEnginePluginProperties;
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
            String site, String region, String userId, UUID serviceId, UUID serviceTemplateId) {
        return resourceManager.getAvailabilityZonesOfRegion(site, region, userId);
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
    public Map<String, String> getEnvVarKeysMappingMap() {
        return Collections.emptyMap();
    }

    @Override
    public ServiceTemplateReviewPluginResultType validateServiceTemplate(Ocl ocl) {
        if (flexibleEnginePluginProperties.getServiceTemplate().getAutoApprove()) {
            return ServiceTemplateReviewPluginResultType.APPROVED;
        }
        return ServiceTemplateReviewPluginResultType.MANUAL_REVIEW_REQUIRED;
    }

    @Override
    public void prepareServiceTemplate(Ocl ocl) {}

    @Override
    public List<String> getSites() {
        return List.of(DEFAULT_SITE);
    }

    @Override
    @Retryable(
            retryFor = UnavailableServiceRegionsException.class,
            maxAttemptsExpression = "${xpanse.http-client-request.retry-max-attempts}",
            backoff =
                    @Backoff(delayExpression = "${xpanse.http-client-request.delay-milliseconds}"))
    public boolean validateRegionsOfService(Ocl ocl) {
        List<String> errors = new ArrayList<>();
        ocl.getCloudServiceProvider()
                .getRegions()
                .forEach(
                        region -> {
                            try {
                                String iamEndpointForRegion =
                                        PROTOCOL_HTTPS
                                                + IAM_ENDPOINT_PREFIX
                                                + region.getName()
                                                + ENDPOINT_SUFFIX;
                                ResponseEntity<JsonNode> response =
                                        restTemplate.getForEntity(
                                                iamEndpointForRegion, JsonNode.class);
                                log.info(
                                        "Request IAM endpoint for region {} get response with"
                                                + " status {} body {}",
                                        region.getName(),
                                        response.getStatusCode().value(),
                                        response.getBody());
                            } catch (RestClientException e) {
                                log.error(
                                        "Request IAM endpoint for region {} error. {}",
                                        region.getName(),
                                        e.getMessage());
                                String errorMsg =
                                        String.format(
                                                "Region with name %s is unavailable in "
                                                        + "Csp %s.",
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
        CredentialVariables accessKey =
                new CredentialVariables(
                        getCsp(),
                        DEFAULT_SITE,
                        CredentialType.VARIABLES,
                        "AK_SK",
                        "The access key and security key.",
                        null,
                        credentialVariables);
        credentialVariables.add(
                new CredentialVariable(
                        FlexibleEngineConstants.OS_ACCESS_KEY, "The access key.", true));
        credentialVariables.add(
                new CredentialVariable(
                        FlexibleEngineConstants.OS_SECRET_KEY, "The security key.", true));
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
        return pricingCalculator.getServiceFlavorPrice(request);
    }
}
