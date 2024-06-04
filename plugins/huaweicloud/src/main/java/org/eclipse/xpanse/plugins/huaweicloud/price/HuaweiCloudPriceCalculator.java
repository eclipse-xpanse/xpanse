/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.price;

import com.huaweicloud.sdk.bss.v2.BssClient;
import com.huaweicloud.sdk.bss.v2.model.DemandProductInfo;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsRequest;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsResponse;
import com.huaweicloud.sdk.bss.v2.model.RateOnDemandReq;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ClientRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.Resource;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceRequest;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/**
 * Class that implements the price calculation for HuaweiCloud.
 */
@Slf4j
@Component
public class HuaweiCloudPriceCalculator {
    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(24L);
    private static final BigDecimal HOURS_PER_MONTH = BigDecimal.valueOf(24 * 30L);
    private static final BigDecimal HOURS_PER_YEAR = BigDecimal.valueOf(24 * 365L);
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    @jakarta.annotation.Resource
    private CredentialCenter credentialCenter;
    @jakarta.annotation.Resource
    private HuaweiCloudClient huaweiCloudClient;
    @jakarta.annotation.Resource
    private HuaweiCloudRetryStrategy huaweiCloudRetryStrategy;
    @jakarta.annotation.Resource
    private HuaweiCloudInternationalPriceCalculator internationalPriceCalculator;


    /**
     * Get the price of the service.
     *
     * @param request service price request
     * @return price
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public FlavorPriceResult getServicePrice(ServicePriceRequest request) {
        if (request.getBillingMode() == BillingMode.PAY_PER_USE) {
            try {
                return getServicePriceWithPayPerUse(request);
            } catch (Exception e) {
                String errorMsg = "Get service price with billingModel Pay per Use error."
                        + e.getMessage();
                int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                        ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
                log.error(errorMsg + " Retry count:" + retryCount);
                throw new ClientApiCallFailedException(errorMsg);
            }
        } else {
            return getServicePriceWithFixed(request);
        }
    }


    private FlavorPriceResult getServicePriceWithPayPerUse(ServicePriceRequest request) {
        ResourceUsage resourceUsage = request.getFlavorRatingMode().getResourceUsage();
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.HUAWEI, CredentialType.VARIABLES,
                        request.getUserId());
        Map<String, String> credentialVariablesMap =
                huaweiCloudClient.getCredentialVariablesMap((CredentialVariables) credential);
        ICredential globalCredential =
                huaweiCloudClient.getGlobalCredential(credentialVariablesMap);
        String projectId =
                huaweiCloudClient.getProjectId(globalCredential, request.getRegionName());
        // Get recurring price with resources usage in HuaweiCloud International Website
        Price recurringPrice = null;
        try {
            recurringPrice =
                    getRecurringPriceWithResourcesUsage(request, globalCredential, projectId);
        } catch (ClientRequestException e) {
            if (e.getErrorCode().equals("CBC.0150") && e.getHttpStatusCode() == 403) {
                log.error("Get the price with resources usage failed. {}", e.getMessage());
                recurringPrice = internationalPriceCalculator
                        .getInternationalRecurringPriceWithResourcesUsage(
                                request, globalCredential, projectId);
            }
        }
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setRecurringPrice(recurringPrice);
        // Add markup price if not null
        Price markUpPrice = resourceUsage.getMarkUpPrice();
        addExtraPaymentPrice(flavorPriceResult, markUpPrice);
        // Add license price if not null
        Price licensePrice = resourceUsage.getLicensePrice();
        addExtraPaymentPrice(flavorPriceResult, licensePrice);
        return flavorPriceResult;
    }

    private Price getRecurringPriceWithResourcesUsage(ServicePriceRequest request,
                                                      ICredential globalCredential,
                                                      String projectId) {
        BssClient bssClient = huaweiCloudClient.getBssClient(globalCredential);
        ListOnDemandResourceRatingsRequest payPerUseRequest =
                convertToPayPerUseRequest(request, projectId);
        ListOnDemandResourceRatingsResponse response =
                bssClient.listOnDemandResourceRatingsInvoker(payPerUseRequest)
                        .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                        .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                        .backoffStrategy(huaweiCloudRetryStrategy)
                        .invoke();
        return convertResourceRatingsResponseToRecurringPrice(response);
    }

    private ListOnDemandResourceRatingsRequest convertToPayPerUseRequest(
            ServicePriceRequest request, String projectId) {
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
            productInfo.setUsageValue(Double.parseDouble(usageValue));
        } else {
            productInfo.setUsageValue(1.0);
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

    private void addExtraPaymentPrice(FlavorPriceResult flavorPriceResult, Price price) {
        if (Objects.nonNull(price)) {
            if (PricingPeriod.ONE_TIME == price.getPeriod()) {
                if (Objects.nonNull(flavorPriceResult.getOneTimePaymentPrice())) {
                    flavorPriceResult.getOneTimePaymentPrice().setCost(
                            flavorPriceResult.getOneTimePaymentPrice().getCost()
                                    .add(price.getCost()));
                } else {
                    Price oneTimePaymentPrice = new Price();
                    oneTimePaymentPrice.setCost(price.getCost());
                    oneTimePaymentPrice.setCurrency(price.getCurrency());
                    oneTimePaymentPrice.setPeriod(PricingPeriod.ONE_TIME);
                    flavorPriceResult.setOneTimePaymentPrice(oneTimePaymentPrice);
                }
            } else {
                BigDecimal costPerHour = getAmountPerHour(price);
                if (Objects.nonNull(flavorPriceResult.getRecurringPrice())) {
                    flavorPriceResult.getRecurringPrice().setCost(
                            flavorPriceResult.getRecurringPrice().getCost().add(costPerHour));
                } else {
                    Price recurringPrice = new Price();
                    recurringPrice.setCost(costPerHour);
                    recurringPrice.setCurrency(price.getCurrency());
                    recurringPrice.setPeriod(price.getPeriod());
                    flavorPriceResult.setRecurringPrice(recurringPrice);
                }
            }
        }
    }

    private BigDecimal getAmountPerHour(Price price) {
        if (Objects.isNull(price) || Objects.isNull(price.getCost())
                || Objects.isNull(price.getPeriod())) {
            return BigDecimal.ZERO;
        }
        BigDecimal periodCost = price.getCost();
        return switch (price.getPeriod()) {
            case YEARLY -> periodCost.divide(HOURS_PER_YEAR, SCALE, ROUNDING_MODE);
            case MONTHLY -> periodCost.divide(HOURS_PER_MONTH, SCALE, ROUNDING_MODE);
            case DAILY -> periodCost.divide(HOURS_PER_DAY, SCALE, ROUNDING_MODE);
            default -> periodCost.setScale(SCALE, ROUNDING_MODE);
        };
    }

    private FlavorPriceResult getServicePriceWithFixed(ServicePriceRequest request) {
        Price fixedPrice = request.getFlavorRatingMode().getFixedPrice();
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setRecurringPrice(fixedPrice);
        return flavorPriceResult;
    }
}