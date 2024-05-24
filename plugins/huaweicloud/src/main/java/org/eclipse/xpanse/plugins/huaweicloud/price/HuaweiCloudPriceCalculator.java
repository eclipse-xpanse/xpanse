/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.price;

import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy.DEFAULT_DELAY_MILLIS;
import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy.DEFAULT_RETRY_TIMES;

import com.huaweicloud.sdk.bss.v2.BssClient;
import com.huaweicloud.sdk.bss.v2.model.DemandProductInfo;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsRequest;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsResponse;
import com.huaweicloud.sdk.bss.v2.model.RateOnDemandReq;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsRequest;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.Resource;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.ServicePrice;
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
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CredentialCenter credentialCenter;
    private final HuaweiCloudClient huaweiCloudClient;


    /**
     * Constructs a HuaweiCloudPriceCalculator with the necessary dependencies.
     */
    @Autowired
    public HuaweiCloudPriceCalculator(CredentialCenter credentialCenter,
                                      HuaweiCloudClient huaweiCloudClient) {
        this.credentialCenter = credentialCenter;
        this.huaweiCloudClient = huaweiCloudClient;
    }


    /**
     * Get the price of the service.
     *
     * @param request service price request
     * @return price
     */
    public ServicePrice getServicePrice(ServicePriceRequest request) {
        if (request.getBillingMode() == BillingMode.PAY_PER_USE) {
            try {
                return getServicePriceWithPayPerUse(request);
            } catch (Exception e) {
                String errorMessage = "Get service price with billingModel Pay per Use error."
                        + e.getMessage();
                throw new ClientApiCallFailedException(errorMessage);
            }
        } else {
            return getServicePriceWithFixed(request);
        }
    }


    private ServicePrice getServicePriceWithPayPerUse(ServicePriceRequest request) {
        ResourceUsage resourceUsage = request.getFlavorRatingMode().getResourceUsage();
        ServicePrice servicePrice = new ServicePrice();
        // Get recurring price with resources usage
        Price recurringPrice = getRecurringPriceWithResourcesUsage(request);
        servicePrice.setRecurringPrice(recurringPrice);
        // Add markup price if not null
        Price markUpPrice = resourceUsage.getMarkUpPrice();
        addExtraPaymentPrice(servicePrice, markUpPrice);
        // Add license price if not null
        Price licensePrice = resourceUsage.getLicensePrice();
        addExtraPaymentPrice(servicePrice, licensePrice);
        return servicePrice;
    }

    private Price getRecurringPriceWithResourcesUsage(ServicePriceRequest request) {
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(Csp.HUAWEI, CredentialType.VARIABLES,
                        request.getUserId());
        Map<String, String> credentialVariablesMap =
                huaweiCloudClient.getCredentialVariablesMap((CredentialVariables) credential);
        ICredential globalCredential =
                huaweiCloudClient.getGlobalCredential(credentialVariablesMap);
        BssClient bssClient = huaweiCloudClient.getBssClient(globalCredential);
        String projectId = getProjectId(globalCredential, request.getRegionName());
        ListOnDemandResourceRatingsRequest payPerUseRequest =
                convertToPayPerUseRequest(request, projectId);
        ListOnDemandResourceRatingsResponse response =
                bssClient.listOnDemandResourceRatingsInvoker(payPerUseRequest)
                        .retryTimes(DEFAULT_RETRY_TIMES)
                        .retryCondition(huaweiCloudClient::matchRetryCondition)
                        .backoffStrategy(new HuaweiCloudRetryStrategy(DEFAULT_DELAY_MILLIS))
                        .invoke();
        return convertResourceRatingsResponseToRecurringPrice(response);
    }

    private String getProjectId(ICredential globalCredential,
                                String regionName) {
        String projectId = null;
        IamClient iamClient = huaweiCloudClient.getIamClient(globalCredential, regionName);
        KeystoneListProjectsRequest listProjectsRequest = new KeystoneListProjectsRequest();
        KeystoneListProjectsResponse listProjectsResponse =
                iamClient.keystoneListProjectsInvoker(listProjectsRequest)
                        .retryTimes(DEFAULT_RETRY_TIMES)
                        .retryCondition(huaweiCloudClient::matchRetryCondition)
                        .backoffStrategy(new HuaweiCloudRetryStrategy(DEFAULT_DELAY_MILLIS))
                        .invoke();
        if (CollectionUtils.isNotEmpty(listProjectsResponse.getProjects())) {
            projectId = listProjectsResponse.getProjects().getFirst().getId();
        }
        return projectId;
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

    private void addExtraPaymentPrice(ServicePrice servicePrice, Price price) {
        if (Objects.nonNull(price)) {
            if (PricingPeriod.ONE_TIME == price.getPeriod()) {
                if (Objects.nonNull(servicePrice.getOneTimePaymentPrice())) {
                    servicePrice.getOneTimePaymentPrice().setCost(
                            servicePrice.getOneTimePaymentPrice().getCost().add(price.getCost()));
                } else {
                    Price oneTimePaymentPrice = new Price();
                    oneTimePaymentPrice.setCost(price.getCost());
                    oneTimePaymentPrice.setCurrency(price.getCurrency());
                    oneTimePaymentPrice.setPeriod(PricingPeriod.ONE_TIME);
                    servicePrice.setOneTimePaymentPrice(oneTimePaymentPrice);
                }
            } else {
                BigDecimal costPerHour = getAmountPerHour(price);
                if (Objects.nonNull(servicePrice.getRecurringPrice())) {
                    servicePrice.getRecurringPrice().setCost(
                            servicePrice.getRecurringPrice().getCost().add(costPerHour));
                } else {
                    Price recurringPrice = new Price();
                    recurringPrice.setCost(costPerHour);
                    recurringPrice.setCurrency(price.getCurrency());
                    recurringPrice.setPeriod(price.getPeriod());
                    servicePrice.setRecurringPrice(recurringPrice);
                }
            }
        }
    }

    private BigDecimal getAmountPerHour(Price price) {
        BigDecimal cost = price.getCost();
        return switch (price.getPeriod()) {
            case YEARLY -> cost.divide(HOURS_PER_YEAR);
            case MONTHLY -> cost.divide(HOURS_PER_MONTH);
            case DAILY -> cost.divide(HOURS_PER_DAY);
            default -> cost;
        };
    }

    private ServicePrice getServicePriceWithFixed(ServicePriceRequest request) {
        Price fixedPrice = request.getFlavorRatingMode().getFixedPrice();
        ServicePrice servicePrice = new ServicePrice();
        servicePrice.setRecurringPrice(fixedPrice);
        return servicePrice;
    }
}