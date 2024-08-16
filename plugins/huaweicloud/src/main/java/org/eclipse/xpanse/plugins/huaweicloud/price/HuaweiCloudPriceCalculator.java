/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.price;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsRequest;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.billing.utils.BillingCommonUtils;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class that implements the price calculation of services in HuaweiCloud.
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
    @jakarta.annotation.Resource
    private HuaweiCloudChinesePriceCalculator chinesePriceCalculator;
    @jakarta.annotation.Resource
    private HuaweiCloudGlobalPriceCalculator globalPriceCalculator;


    /**
     * Get the price of the service.
     *
     * @param request service price request
     * @return price
     */
    @Retryable(retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public FlavorPriceResult getServiceFlavorPrice(ServiceFlavorPriceRequest request) {
        if (request.getBillingMode() == BillingMode.PAY_PER_USE) {
            return getServiceFlavorPriceWithPayPerUse(request);
        } else {
            return getServiceFlavorPriceWithFixed(request);
        }
    }


    private FlavorPriceResult getServiceFlavorPriceWithPayPerUse(
            ServiceFlavorPriceRequest request) {
        ResourceUsage resourceUsage = request.getFlavorRatingMode().getResourceUsage();
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.HUAWEI_CLOUD, CredentialType.VARIABLES,
                        request.getUserId());
        Map<String, String> credentialVariablesMap =
                huaweiCloudClient.getCredentialVariablesMap((CredentialVariables) credential);
        ICredential globalCredential =
                huaweiCloudClient.getGlobalCredential(credentialVariablesMap);
        Price recurringPrice;
        try {
            recurringPrice = getPriceWithResourcesUsage(request, globalCredential);
        } catch (Exception e) {
            log.error("Get price with resources usage failed.{}", e.getMessage());
            huaweiCloudRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setRecurringPrice(recurringPrice);
        // Add markup price if not null
        Price markUpPrice = BillingCommonUtils.getSpecificPriceByRegion(
                resourceUsage.getMarkUpPrices(), request.getRegionName());
        addExtraPaymentPrice(flavorPriceResult, markUpPrice);
        // Add license price if not null
        Price licensePrice = BillingCommonUtils.getSpecificPriceByRegion(
                resourceUsage.getLicensePrices(), request.getRegionName());
        addExtraPaymentPrice(flavorPriceResult, licensePrice);
        return flavorPriceResult;
    }

    private Price getPriceWithResourcesUsage(ServiceFlavorPriceRequest request,
                                             ICredential globalCredential) {

        String projectId = null;
        try {
            projectId = getProjectId(globalCredential, request.getRegionName());
        } catch (Exception e) {
            log.error("Get project id with region {} failed. {}",
                    request.getRegionName(), e.getMessage());
        }
        if (Objects.nonNull(projectId)) {
            try {
                log.info("Trying to get price with resources usage by international website.");
                return internationalPriceCalculator
                        .getPriceWithResourcesUsageInInternationalWebsite(
                                request, globalCredential, projectId);
            } catch (Exception e) {
                log.error("Failed to get price with resources usage in international website. {}",
                        e.getMessage());
            }
            try {
                log.info("Trying to get price with resources usage by chinese website.");
                return chinesePriceCalculator.getPriceWithResourcesUsageInChineseWebsite(request,
                        globalCredential, projectId);
            } catch (Exception e) {
                log.error("Failed to get price with resources usage in chinese website. {}",
                        e.getMessage());
            }
        }
        try {
            log.info("Trying to get price with resources usage by global rest api.");
            return globalPriceCalculator.getPriceWithResourcesUsageByGlobalRestApi(request);
        } catch (Exception e) {
            log.error("Failed to get price with resources usage by global rest api. {}",
                    e.getMessage());
            throw new ClientApiCallFailedException(
                    "Failed to get price with resources usage by all methods.");
        }
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
                    recurringPrice.setPeriod(PricingPeriod.HOURLY);
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

    private FlavorPriceResult getServiceFlavorPriceWithFixed(ServiceFlavorPriceRequest request) {
        Price fixedPrice = BillingCommonUtils.getSpecificPriceByRegion(
                request.getFlavorRatingMode().getFixedPrices(), request.getRegionName());
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setRecurringPrice(fixedPrice);
        return flavorPriceResult;
    }

    /**
     * Get projectId with region.
     *
     * @param globalCredential ICredential
     * @param regionName       region name.
     * @return projectId.
     */
    private String getProjectId(ICredential globalCredential, String regionName) {
        String projectId = null;
        try {
            IamClient iamClient = huaweiCloudClient.getIamClient(globalCredential, regionName);
            KeystoneListProjectsRequest listProjectsRequest =
                    new KeystoneListProjectsRequest().withName(regionName);
            KeystoneListProjectsResponse listProjectsResponse =
                    iamClient.keystoneListProjectsInvoker(listProjectsRequest)
                            .retryTimes(huaweiCloudRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(huaweiCloudRetryStrategy::matchRetryCondition)
                            .backoffStrategy(huaweiCloudRetryStrategy)
                            .invoke();
            if (!CollectionUtils.isEmpty(listProjectsResponse.getProjects())) {
                projectId = listProjectsResponse.getProjects().getFirst().getId();
            }
            return projectId;
        } catch (Exception e) {
            log.error("Get project id with region {} failed.", regionName);
            huaweiCloudRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }
}