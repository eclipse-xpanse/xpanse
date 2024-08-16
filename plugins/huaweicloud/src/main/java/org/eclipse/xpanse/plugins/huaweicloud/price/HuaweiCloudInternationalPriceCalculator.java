/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.price;

import com.huaweicloud.sdk.bssintl.v2.BssintlClient;
import com.huaweicloud.sdk.bssintl.v2.model.DemandProductInfo;
import com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsRequest;
import com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsResponse;
import com.huaweicloud.sdk.bssintl.v2.model.RateOnDemandReq;
import com.huaweicloud.sdk.core.auth.ICredential;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.Resource;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy;
import org.springframework.stereotype.Component;

/**
 * Class that implements the price calculation for the international website of HuaweiCloud.
 */
@Slf4j
@Component
public class HuaweiCloudInternationalPriceCalculator {

    @jakarta.annotation.Resource
    private HuaweiCloudClient huaweiCloudClient;
    @jakarta.annotation.Resource
    private HuaweiCloudRetryStrategy huaweiCloudRetryStrategy;


    /**
     * Get the price of the service in the international website.
     *
     * @param request          ServiceFlavorPriceRequest
     * @param globalCredential globalCredential
     * @param projectId        projectId
     * @return Price in the international website.
     */
    public Price getPriceWithResourcesUsageInInternationalWebsite(ServiceFlavorPriceRequest request,
                                                                  ICredential globalCredential,
                                                                  String projectId) {
        try {
            BssintlClient bssintlClient = huaweiCloudClient.getBssintlClient(globalCredential);
            ListOnDemandResourceRatingsRequest payPerUseRequest =
                    convertToPayPerUseRequest(request, projectId);
            ListOnDemandResourceRatingsResponse response =
                    bssintlClient.listOnDemandResourceRatingsInvoker(payPerUseRequest)
                            .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                            .backoffStrategy(huaweiCloudRetryStrategy)
                            .invoke();
            return convertResourceRatingsResponseToRecurringPrice(response);
        } catch (Exception e) {
            huaweiCloudRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    private ListOnDemandResourceRatingsRequest convertToPayPerUseRequest(
            ServiceFlavorPriceRequest request, String projectId) {
        RateOnDemandReq rateOnDemandReq = new RateOnDemandReq();
        rateOnDemandReq.setProjectId(projectId);
        rateOnDemandReq.setInquiryPrecision(0);
        List<DemandProductInfo> productInfos =
                request.getFlavorRatingMode().getResourceUsage().getResources().stream()
                        .map(resource -> convertToDemandProductInfo(resource,
                                request.getRegionName())).toList();
        rateOnDemandReq.setProductInfos(productInfos);
        ListOnDemandResourceRatingsRequest payPerUseRequest =
                new ListOnDemandResourceRatingsRequest();
        payPerUseRequest.setBody(rateOnDemandReq);
        return payPerUseRequest;
    }

    private DemandProductInfo convertToDemandProductInfo(Resource resource, String region) {
        DemandProductInfo productInfo = new DemandProductInfo();
        productInfo.setId(UUID.randomUUID().toString());
        productInfo.setRegion(region);
        // usageMeasureId: the usage measurement unit, 4 means hour.
        productInfo.setUsageMeasureId(4);
        productInfo.setSubscriptionNum(resource.getCount());
        String cloudServiceType = resource.getProperties().get("cloud_service_type");
        productInfo.setCloudServiceType(cloudServiceType);
        String resourceSpecCode = resource.getProperties().get("resource_spec");
        productInfo.setResourceSpec(resourceSpecCode);
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
            productInfo.setSizeMeasureId(Integer.parseInt(sizeMeasureId));
        }
        String usageFactor = resource.getProperties().get("usage_factor");
        if (StringUtils.isNotBlank(usageFactor)) {
            productInfo.setUsageFactor(usageFactor);
        } else {
            productInfo.setUsageFactor("Duration");
        }
        String usageValue = resource.getProperties().get("usage_value");
        if (StringUtils.isNotBlank(usageValue)) {
            productInfo.setUsageValue(new BigDecimal(usageValue));
        } else {
            productInfo.setUsageValue(BigDecimal.valueOf(1.0));
        }
        return productInfo;
    }

    private Price convertResourceRatingsResponseToRecurringPrice(
            ListOnDemandResourceRatingsResponse response) {
        Price recurringPrice = new Price();
        recurringPrice.setCost(response.getAmount());
        recurringPrice.setCurrency(Currency.getByValue(response.getCurrency()));
        recurringPrice.setPeriod(PricingPeriod.HOURLY);
        return recurringPrice;
    }
}