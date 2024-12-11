/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.regiocloud;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.REGION_AZS_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.SERVICE_FLAVOR_PRICE_CACHE_NAME;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.OS_AUTH_URL;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.REGIO_CLOUD_AUTH_URL;

import jakarta.annotation.Resource;
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
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.UnavailableServiceRegionsException;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.audit.AuditLog;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.openstack.common.auth.ProviderAuthInfoResolver;
import org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants;
import org.eclipse.xpanse.plugins.openstack.common.manage.OpenstackResourceManager;
import org.eclipse.xpanse.plugins.openstack.common.manage.OpenstackServersManager;
import org.eclipse.xpanse.plugins.openstack.common.price.OpenstackServicePriceCalculator;
import org.eclipse.xpanse.plugins.openstack.common.resourcehandler.OpenstackTerraformResourceHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** OrchestratorPlugin implementation for the provider RegionCloud. */
@Slf4j
@Component
public class RegioCloudOrchestratorPlugin implements OrchestratorPlugin {
    private static final String DEFAULT_SITE = "default";
    @Resource private OpenstackTerraformResourceHandler terraformResourceHandler;
    @Resource private OpenstackServersManager serversManager;
    @Resource private OpenstackResourceManager resourceManager;
    @Resource private OpenstackServicePriceCalculator priceCalculator;
    @Resource private ProviderAuthInfoResolver providerAuthInfoResolver;

    @Value("${regiocloud.auto.approve.service.template.enabled:false}")
    private boolean autoApproveServiceTemplateEnabled;

    @Override
    public Csp getCsp() {
        return Csp.REGIO_CLOUD;
    }

    @Override
    public List<String> requiredProperties() {
        return List.of(providerAuthInfoResolver.getAuthUrlKeyByCsp(getCsp()));
    }

    @Override
    public Map<String, String> getEnvVarKeysMappingMap() {
        Map<String, String> envVarKeysMappingMap = new HashMap<>();
        envVarKeysMappingMap.put(OS_AUTH_URL, REGIO_CLOUD_AUTH_URL);
        return envVarKeysMappingMap;
    }

    @Override
    public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
        Map<DeployerKind, DeployResourceHandler> resourceHandlers = new HashMap<>();
        resourceHandlers.put(DeployerKind.TERRAFORM, terraformResourceHandler);
        resourceHandlers.put(DeployerKind.OPEN_TOFU, terraformResourceHandler);
        return resourceHandlers;
    }

    @Override
    public boolean autoApproveServiceTemplateIsEnabled() {
        return autoApproveServiceTemplateEnabled;
    }

    @Override
    public List<String> getSites() {
        return List.of(DEFAULT_SITE);
    }

    @Override
    public boolean validateRegionsOfService(Ocl ocl) {
        List<String> errors = new ArrayList<>();
        ocl.getCloudServiceProvider()
                .getRegions()
                .forEach(
                        region -> {
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
                        OpenstackCommonEnvironmentConstants.PROJECT,
                        "The Name of the Tenant or Project to use.",
                        true,
                        false));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.USERNAME,
                        "The Username to login with.",
                        true,
                        false));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.PASSWORD,
                        "The Password to login with.",
                        true,
                        true));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.DOMAIN,
                        "The domain of the RegionCloud installation to be used.",
                        true,
                        false));
        CredentialVariables httpAuth =
                new CredentialVariables(
                        getCsp(),
                        DEFAULT_SITE,
                        CredentialType.VARIABLES,
                        "USERNAME_PASSWORD",
                        "Authenticate at the specified URL using an account and password.",
                        null,
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

    @Override
    public List<String> getExistingResourceNamesWithKind(
            String site, String region, String userId, DeployResourceKind kind, UUID serviceId) {
        return resourceManager.getExistingResourceNamesWithKind(
                getCsp(), site, region, userId, kind, serviceId);
    }

    @Override
    @Cacheable(cacheNames = REGION_AZS_CACHE_NAME)
    public List<String> getAvailabilityZonesOfRegion(
            String site, String region, String userId, UUID serviceId, UUID serviceTemplateId) {
        return resourceManager.getAvailabilityZonesOfRegion(
                getCsp(), site, region, userId, serviceId, serviceTemplateId);
    }

    @Override
    public List<Metric> getMetricsForResource(ResourceMetricsRequest resourceMetricRequest) {
        return Collections.emptyList();
    }

    @Override
    public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
        return Collections.emptyList();
    }

    @Override
    public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
        return serversManager.startService(getCsp(), serviceStateManageRequest);
    }

    @Override
    public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
        return serversManager.stopService(getCsp(), serviceStateManageRequest);
    }

    @Override
    public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
        return serversManager.restartService(getCsp(), serviceStateManageRequest);
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
