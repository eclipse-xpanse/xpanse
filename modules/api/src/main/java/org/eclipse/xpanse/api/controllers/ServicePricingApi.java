/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.ServicePrice;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.exceptions.ServicePriceCalculationFailed;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceRequest;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface methods for managing cloud resources.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServicePricingApi {

    @Resource
    private PluginManager pluginManager;

    @Resource
    private UserServiceHelper userServiceHelper;

    @Resource
    private DatabaseServiceTemplateStorage templateStorage;

    /**
     * List existing cloud resources based on type.
     */
    @Tag(name = "CloudResources",
            description = "API to view cloud resources by type")
    @GetMapping(value = "/pricing/{templateId}/{region}/{flavorName}/{billingMode}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List existing cloud resource names with kind")
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public ServicePrice getServicePriceByFlavor(
            @Parameter(name = "templateId", description = "id of the service template")
            @PathVariable(name = "templateId") String templateId,
            @Parameter(name = "region", description = "region name of the service")
            @PathVariable(name = "region") String region,
            @Parameter(name = "flavorName", description = "flavor name of the service")
            @PathVariable("flavorName") String flavorName,
            @Parameter(name = "billingMode", description = "mode of billing")
            @PathVariable("billingMode") BillingMode billingMode) {
        ServiceTemplateEntity serviceTemplate = getServiceTemplate(templateId);
        RatingMode flavorPriceMode =
                getServiceFlavorRatingMode(serviceTemplate, flavorName, billingMode);
        ServicePriceRequest servicePriceRequest =
                getServicePriceRequest(flavorPriceMode, region, billingMode);
        Csp csp = serviceTemplate.getCsp();
        OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
        return orchestratorPlugin.getServicePrice(servicePriceRequest);
    }


    private ServicePriceRequest getServicePriceRequest(RatingMode flavorPriceMode,
                                                       String region,
                                                       BillingMode billingMode) {
        ServicePriceRequest servicePriceRequest = new ServicePriceRequest();
        servicePriceRequest.setUserId(userServiceHelper.getCurrentUserId());
        servicePriceRequest.setRegionName(region);
        servicePriceRequest.setFlavorRatingMode(flavorPriceMode);
        servicePriceRequest.setBillingMode(billingMode);
        return servicePriceRequest;
    }

    private ServiceTemplateEntity getServiceTemplate(String templateId) {
        ServiceTemplateEntity serviceTemplate =
                templateStorage.getServiceTemplateById(UUID.fromString(templateId));
        if (Objects.isNull(serviceTemplate)) {
            String errMsg = String.format("Service template with id %s not found.", templateId);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        return serviceTemplate;
    }

    private RatingMode getServiceFlavorRatingMode(ServiceTemplateEntity serviceTemplate,
                                                  String flavorName, BillingMode billingMode) {
        Optional<ServiceFlavorWithPrice> flavorOptional =
                serviceTemplate.getOcl().getFlavors().getServiceFlavors().stream()
                        .filter(serviceFlavor -> serviceFlavor.getName().equals(flavorName))
                        .findFirst();
        if (flavorOptional.isEmpty()) {
            String errMsg = String.format("Flavor %s not found in service template with id %s.",
                    flavorName, serviceTemplate.getId());
            throw new ServicePriceCalculationFailed(errMsg);
        }
        if (Objects.isNull(flavorOptional.get().getPricing())) {
            String errMsg = String.format("Flavor %s in service template with id %s has no "
                    + "pricing.", flavorName, serviceTemplate.getId());
            throw new ServicePriceCalculationFailed(errMsg);
        }
        RatingMode flavorPriceMode = flavorOptional.get().getPricing();
        ResourceUsage resourceUsage = flavorPriceMode.getResourceUsage();
        if (BillingMode.FIXED.equals(billingMode) && Objects.isNull(resourceUsage)) {
            String errorMsg = "BillingMode 'Pay-Per-Use' can not be supported due to "
                    + "the 'ResourceUsage' is null.";
            throw new ServicePriceCalculationFailed(errorMsg);
        }
        Price fixedPrice = flavorPriceMode.getFixedPrice();
        if (BillingMode.PAY_PER_USE.equals(billingMode) && Objects.isNull(fixedPrice)) {

            String errorMsg = "BillingMode 'Fixed' can not be supported due to "
                    + "the 'FixedPrice' is null.";
            throw new ServicePriceCalculationFailed(errorMsg);
        }
        return flavorPriceMode;
    }
}
