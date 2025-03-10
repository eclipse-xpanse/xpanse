/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.deployment.DeployResultManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks.OpenTofuDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.ReFetchResult;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;

/** Bean to manage task result via tofu-maker. */
@Slf4j
@Component
public class TofuMakerResultReFetchManager {

    @Resource
    private OpenTofuDeploymentResultCallbackManager openTofuDeploymentResultCallbackManager;

    @Resource private DeployResultManager deployResultManager;
    @Resource private TofuMakerApiStoredResultsFetcher tofuMakerApiStoredResultsFetcher;

    /** retrieve openTofu result. */
    public void retrieveOpenTofuResult(ServiceOrderEntity serviceOrder) {
        if (Objects.isNull(serviceOrder)) {
            return;
        }
        try {
            ReFetchResult reFetchResult =
                    tofuMakerApiStoredResultsFetcher.reFetchResultByOrderId(
                            serviceOrder.getOrderId());
            handleReFetchedResult(reFetchResult, serviceOrder);
        } catch (RestClientException e) {
            log.error(
                    "Failed to reFetch results of orders from tofu-maker. OrderId {}. Error message"
                            + " {}.",
                    serviceOrder.getOrderId(),
                    e.getMessage());
        }
    }

    /**
     * batch retrieve openTofu result.
     *
     * @param serviceOrders service orders.
     */
    public void batchRetrieveOpenTofuResults(List<ServiceOrderEntity> serviceOrders) {
        if (CollectionUtils.isEmpty(serviceOrders)) {
            return;
        }
        Map<UUID, ServiceOrderEntity> serviceOrdersMap = new HashMap<>();
        serviceOrders.forEach(
                serviceOrder -> {
                    if (Objects.nonNull(serviceOrder)) {
                        serviceOrdersMap.put(serviceOrder.getOrderId(), serviceOrder);
                    }
                });
        List<UUID> orderIds = new ArrayList<>(serviceOrdersMap.keySet());
        if (CollectionUtils.isEmpty(orderIds)) {
            return;
        }
        try {
            List<ReFetchResult> reFetchResults =
                    tofuMakerApiStoredResultsFetcher.batchReFetchResultsByOrderIds(orderIds);
            reFetchResults.forEach(
                    reFetchResult -> {
                        ServiceOrderEntity serviceOrder =
                                serviceOrdersMap.get(reFetchResult.getRequestId());
                        handleReFetchedResult(reFetchResult, serviceOrder);
                    });
        } catch (RestClientException e) {
            log.error(
                    "Failed to reFetch results of orders from tofu-maker. OrderIds {}. Error"
                            + " message {}.",
                    orderIds,
                    e.getMessage());
        }
    }

    private void handleReFetchedResult(
            ReFetchResult reFetchResult, ServiceOrderEntity serviceOrder) {
        log.info(
                "Handle reFetched result {} of order {}", reFetchResult, serviceOrder.getOrderId());
        if (reFetchResult.getState() == ReFetchResult.StateEnum.OK
                && Objects.nonNull(reFetchResult.getOpenTofuResult())) {
            openTofuDeploymentResultCallbackManager.orderCallback(
                    reFetchResult.getRequestId(), reFetchResult.getOpenTofuResult());
        } else if (reFetchResult.getState() == ReFetchResult.StateEnum.ORDER_IN_PROGRESS) {
            log.info("Order {} is in progress.", serviceOrder.getOrderId());
        } else {
            ServiceDeploymentEntity serviceDeployment = serviceOrder.getServiceDeploymentEntity();
            deployResultManager.saveDeploymentResultWhenErrorReceived(
                    serviceDeployment,
                    serviceOrder,
                    ErrorType.TERRA_BOOT_REQUEST_FAILED,
                    reFetchResult.getErrorMessage());
        }
    }
}
