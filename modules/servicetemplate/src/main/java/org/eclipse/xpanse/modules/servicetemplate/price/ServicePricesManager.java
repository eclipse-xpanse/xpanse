/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate.price;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.PriceWithRegion;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.exceptions.ServicePriceCalculationFailed;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** Implement Interface to manage service prices. */
@Slf4j
@Service
public class ServicePricesManager {

    @Resource private PluginManager pluginManager;

    @Resource private UserServiceHelper userServiceHelper;

    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Get the price of one specific flavor of the service.
     *
     * @param serviceTemplateId service template id
     * @param regionName region name
     * @param siteName site name
     * @param billingMode billing mode
     * @param flavorName flavor name
     * @return FlavorPriceResult
     */
    public FlavorPriceResult getServicePriceByFlavor(
            UUID serviceTemplateId,
            String regionName,
            String siteName,
            BillingMode billingMode,
            String flavorName) {
        ServiceTemplateEntity serviceTemplate = getServiceTemplate(serviceTemplateId);
        RatingMode flavorPriceMode = getServiceFlavorRatingMode(serviceTemplate, flavorName);
        validateFlavorPriceMode(flavorPriceMode, billingMode);
        ServiceFlavorPriceRequest serviceFlavorPriceRequest =
                getServiceFlavorPriceRequest(
                        serviceTemplateId,
                        flavorName,
                        flavorPriceMode,
                        regionName,
                        siteName,
                        billingMode);
        Csp csp = serviceTemplate.getCsp();
        OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
        return orchestratorPlugin.getServiceFlavorPrice(serviceFlavorPriceRequest);
    }

    /**
     * Get the price of all flavors of the service.
     *
     * @param serviceTemplateId service template id
     * @param regionName region name
     * @param siteName site name
     * @param billingMode billing mode
     * @return list of FlavorPriceResult
     */
    public List<FlavorPriceResult> getPricesByService(
            UUID serviceTemplateId, String regionName, String siteName, BillingMode billingMode) {
        ServiceTemplateEntity serviceTemplate = getServiceTemplate(serviceTemplateId);
        List<ServiceFlavorWithPrice> flavors =
                serviceTemplate.getOcl().getFlavors().getServiceFlavors();
        Csp csp = serviceTemplate.getCsp();
        OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
        List<FlavorPriceResult> priceResults = new ArrayList<>();
        for (ServiceFlavorWithPrice flavor : flavors) {
            ServiceFlavorPriceRequest serviceFlavorPriceRequest =
                    getServiceFlavorPriceRequest(
                            serviceTemplateId,
                            flavor.getName(),
                            flavor.getPricing(),
                            regionName,
                            siteName,
                            billingMode);
            FlavorPriceResult flavorPriceResult;
            try {
                validateFlavorPriceMode(flavor.getPricing(), billingMode);
                flavorPriceResult =
                        orchestratorPlugin.getServiceFlavorPrice(serviceFlavorPriceRequest);
            } catch (Exception e) {
                log.error(
                        "Get price of service flavor {} failed. {}",
                        flavor.getName(),
                        e.getMessage());
                flavorPriceResult = new FlavorPriceResult();
                flavorPriceResult.setFlavorName(flavor.getName());
                flavorPriceResult.setBillingMode(billingMode);
                flavorPriceResult.setSuccessful(false);
                flavorPriceResult.setErrorMessage(e.getMessage());
            }
            priceResults.add(flavorPriceResult);
        }
        return priceResults;
    }

    private ServiceFlavorPriceRequest getServiceFlavorPriceRequest(
            UUID serviceTemplateId,
            String flavorName,
            RatingMode flavorPriceMode,
            String regionName,
            String siteName,
            BillingMode billingMode) {
        ServiceFlavorPriceRequest serviceFlavorPriceRequest = new ServiceFlavorPriceRequest();
        serviceFlavorPriceRequest.setUserId(userServiceHelper.getCurrentUserId());
        serviceFlavorPriceRequest.setServiceTemplateId(serviceTemplateId);
        serviceFlavorPriceRequest.setFlavorName(flavorName);
        serviceFlavorPriceRequest.setRegionName(regionName);
        serviceFlavorPriceRequest.setSiteName(siteName);
        serviceFlavorPriceRequest.setFlavorRatingMode(flavorPriceMode);
        serviceFlavorPriceRequest.setBillingMode(billingMode);
        return serviceFlavorPriceRequest;
    }

    private ServiceTemplateEntity getServiceTemplate(UUID serviceTemplateId) {
        ServiceTemplateEntity serviceTemplate =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
        if (Objects.isNull(serviceTemplate)) {
            String errMsg =
                    String.format("Service template with id %s not found.", serviceTemplateId);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        return serviceTemplate;
    }

    private RatingMode getServiceFlavorRatingMode(
            ServiceTemplateEntity serviceTemplate, String flavorName) {
        Optional<ServiceFlavorWithPrice> flavorOptional =
                serviceTemplate.getOcl().getFlavors().getServiceFlavors().stream()
                        .filter(serviceFlavor -> serviceFlavor.getName().equals(flavorName))
                        .findFirst();
        if (flavorOptional.isEmpty()) {
            String errMsg =
                    String.format(
                            "Flavor %s not found in service template with id %s.",
                            flavorName, serviceTemplate.getId());
            throw new ServicePriceCalculationFailed(errMsg);
        }
        if (Objects.isNull(flavorOptional.get().getPricing())) {
            String errMsg =
                    String.format(
                            "Flavor %s in service template with id %s has no " + "pricing.",
                            flavorName, serviceTemplate.getId());
            throw new ServicePriceCalculationFailed(errMsg);
        }
        return flavorOptional.get().getPricing();
    }

    private void validateFlavorPriceMode(RatingMode flavorPriceMode, BillingMode billingMode) {
        ResourceUsage resourceUsage = flavorPriceMode.getResourceUsage();
        if (BillingMode.PAY_PER_USE.equals(billingMode) && Objects.isNull(resourceUsage)) {
            String errorMsg =
                    "BillingMode 'Pay-Per-Use' can not be supported due to "
                            + "the 'ResourceUsage' is null.";
            throw new ServicePriceCalculationFailed(errorMsg);
        }
        List<PriceWithRegion> fixedPrices = flavorPriceMode.getFixedPrices();
        if (BillingMode.FIXED.equals(billingMode) && CollectionUtils.isEmpty(fixedPrices)) {

            String errorMsg =
                    "BillingMode 'Fixed' can not be supported due to "
                            + "the 'FixedPrices' is null.";
            throw new ServicePriceCalculationFailed(errorMsg);
        }
    }
}
