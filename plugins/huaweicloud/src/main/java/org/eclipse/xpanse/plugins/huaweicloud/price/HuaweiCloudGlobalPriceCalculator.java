/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.price;

import com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.Resource;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.plugins.huaweicloud.price.model.ProductInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Class that implements the price calculation for the global rest apis of HuaweiCloud.
 */
@Slf4j
@Component
public class HuaweiCloudGlobalPriceCalculator {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${huaweicloud.price.calculator.global.restapi.url:https://portal-intl.huaweicloud.com"
            + "/api/cbc/global/rest/BSS/billing/ratingservice/v2/inquiry/resource}")
    private String huaweiCloudPriceCalculatorGlobalRestApiUrl;

    /**
     * Get the price of the service with global rest api.
     *
     * @param request ServiceFlavorPriceRequest
     * @return Price in the international website.
     */
    public Price getPriceWithResourcesUsageByGlobalRestApi(ServiceFlavorPriceRequest request) {
        try {
            Map<String, Object> requestBody = getPriceRequestByGlobalRestApi(request);
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            log.info("Request Method: {} Url: {} Request Body: {}.", HttpMethod.POST,
                    huaweiCloudPriceCalculatorGlobalRestApiUrl, requestBody);
            ResponseEntity<ListOnDemandResourceRatingsResponse> responseEntity =
                    restTemplate.exchange(huaweiCloudPriceCalculatorGlobalRestApiUrl,
                            HttpMethod.POST, requestEntity,
                            ListOnDemandResourceRatingsResponse.class
                    );
            log.info("Call the global rest api to calculate the price successfully. "
                            + "Status Code: {}, Response Body: {}", responseEntity.getStatusCode(),
                    responseEntity.getBody());
            return convertResourceRatingsResponseToRecurringPrice(responseEntity.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String errorMessage = e.getResponseBodyAsString();
            log.error("Call the global rest api to calculate the price failed. Error: "
                    + "Status Code {}, Response Body: {}", statusCode, errorMessage);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    private Price convertResourceRatingsResponseToRecurringPrice(
            ListOnDemandResourceRatingsResponse response) {
        if (Objects.isNull(response)) {
            throw new ClientApiCallFailedException("The response of the global rest api is null.");
        }
        Price recurringPrice = new Price();
        recurringPrice.setCost(response.getAmount());
        recurringPrice.setCurrency(Currency.getByValue(response.getCurrency()));
        recurringPrice.setPeriod(PricingPeriod.HOURLY);
        return recurringPrice;
    }

    private Map<String, Object> getPriceRequestByGlobalRestApi(ServiceFlavorPriceRequest request) {
        String region = request.getRegionName();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("regionId", region);
        // billingMode: 1 means pay by usage.
        requestBody.put("chargingMode", 1);
        // periodType: 4 means hourly.
        requestBody.put("periodType", 4);
        requestBody.put("periodNum", 1);
        requestBody.put("subscriptionNum", 1);
        requestBody.put("siteCode", getSiteCodeByRegionName(region));
        List<Resource> resources = request.getFlavorRatingMode().getResourceUsage().getResources();
        ProductInfo[] productInfos = new ProductInfo[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            productInfos[i] = convertToProductInfo(resources.get(i));
        }
        requestBody.put("productInfos", productInfos);
        return requestBody;
    }

    private String getSiteCodeByRegionName(String regionName) {
        return switch (regionName) {
            case "eu-west-0" -> "ALLY_FLEXIBLEENGINE";
            case "eu-west-101" -> "ALLY_HWCEU";
            case "ae-ad-1" -> "ALLY_G42Cloud";
            case "tr-central-201" -> "ALLY_ANKARAPUR";
            case "my-kualalumpur-1" -> "ALLY_TMOne";
            default -> "HWC";
        };
    }

    private ProductInfo convertToProductInfo(Resource resource) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(UUID.randomUUID().toString());
        productInfo.setProductNum(resource.getCount());
        // usageMeasureId: the usage measurement unit, 4 means hour.
        productInfo.setUsageMeasureId(4);
        String cloudServiceType = resource.getProperties().get("cloud_service_type");
        productInfo.setCloudServiceType(cloudServiceType);
        String resourceSpecCode = resource.getProperties().get("resource_spec");
        productInfo.setResourceSpecCode(resourceSpecCode);
        String resourceType = resource.getProperties().get("resource_type");
        productInfo.setResourceType(resourceType);
        String resourceSize = resource.getProperties().get("resource_size");
        if (StringUtils.isNotBlank(resourceSize)) {
            productInfo.setResourceSize(Integer.parseInt(resourceSize));
        } else {
            productInfo.setResourceSize(resource.getCount());
        }
        String sizeMeasureId = resource.getProperties().get("size_measure_id");
        if (StringUtils.isNotBlank(sizeMeasureId)) {
            productInfo.setResourceSizeMeasureId(Integer.parseInt(sizeMeasureId));
        }
        String usageFactor = resource.getProperties().get("usage_factor");
        if (StringUtils.isNotBlank(usageFactor)) {
            productInfo.setUsageFactor(usageFactor);
        } else {
            productInfo.setUsageFactor("Duration");
        }
        String usageValue = resource.getProperties().get("usage_value");
        if (StringUtils.isNotBlank(usageValue)) {
            productInfo.setUsageValue(new BigDecimal(usageValue).toString());
        } else {
            productInfo.setUsageValue(BigDecimal.valueOf(1.0).toString());
        }
        return productInfo;
    }
}