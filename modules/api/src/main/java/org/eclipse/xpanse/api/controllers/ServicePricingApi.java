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
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.servicetemplate.price.ServicePricesManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Value("${service.flavor.price.cache.expire.time.in.minutes:60}")
    private long duration;

    @Resource
    private ServicePricesManager servicePricesManager;


    /**
     * Get the price of one specific flavor of the service.
     */
    @Tag(name = "ServicePrices",
            description = "API to manage prices of the flavors of the service.")
    @GetMapping(value = "/pricing/{templateId}/{region}/{billingMode}/{flavorName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the price of one specific flavor of the service.")
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public ResponseEntity<FlavorPriceResult> getServicePriceByFlavor(
            @Parameter(name = "templateId", description = "id of the service template")
            @PathVariable(name = "templateId") String templateId,
            @Parameter(name = "region", description = "region name of the service")
            @PathVariable(name = "region") String region,
            @Parameter(name = "billingMode", description = "mode of billing")
            @PathVariable("billingMode") BillingMode billingMode,
            @Parameter(name = "flavorName", description = "flavor name of the service")
            @PathVariable("flavorName") String flavorName) {
        try {
            FlavorPriceResult flavorPriceResult = servicePricesManager
                    .getServicePriceByFlavor(templateId, region, billingMode, flavorName);
            return ResponseEntity.ok().cacheControl(getCacheControl()).body(flavorPriceResult);
        } catch (Exception ex) {
            FlavorPriceResult errorResult = new FlavorPriceResult();
            errorResult.setSuccessful(false);
            errorResult.setBillingMode(billingMode);
            errorResult.setFlavorName(flavorName);
            errorResult.setErrorMessage(ex.getMessage());
            log.error("Error fetching prices of the flavor of the service.", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .cacheControl(CacheControl.noCache()).body(errorResult);
        }
    }


    /**
     * Get the prices of all flavors of the service.
     */
    @Tag(name = "ServicePrices",
            description = "API to manage prices of the flavors of the service.")
    @GetMapping(value = "/pricing/service/{templateId}/{region}/{billingMode}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the prices of all flavors of the service")
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public ResponseEntity<List<FlavorPriceResult>> getPricesByService(
            @Parameter(name = "templateId", description = "id of the service template")
            @PathVariable(name = "templateId") String templateId,
            @Parameter(name = "region", description = "region name of the service")
            @PathVariable(name = "region") String region,
            @Parameter(name = "billingMode", description = "mode of billing")
            @PathVariable("billingMode") BillingMode billingMode) {
        List<FlavorPriceResult> allFlavorPriceResult =
                servicePricesManager.getPricesByService(templateId, region, billingMode);
        boolean findFailed = allFlavorPriceResult.stream()
                .anyMatch(flavorPriceResult -> !flavorPriceResult.isSuccessful());
        if (findFailed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .cacheControl(CacheControl.noCache()).body(allFlavorPriceResult);
        }
        return ResponseEntity.ok().cacheControl(getCacheControl()).body(allFlavorPriceResult);
    }


    private CacheControl getCacheControl() {
        long durationTime = this.duration > 0 ? this.duration : 60;
        return CacheControl.maxAge(durationTime, TimeUnit.MINUTES).mustRevalidate();
    }

}
