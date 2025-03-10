/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

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
import org.eclipse.xpanse.modules.deployment.deployers.terraform.callbacks.TerraformDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.ReFetchResult;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;

/** Bean to manage task result via terra-boot. */
@Slf4j
@Component
public class TerraBootResultReFetchManager {

    @Resource
    private TerraformDeploymentResultCallbackManager terraformDeploymentResultCallbackManager;

    @Resource private DeployResultManager deployResultManager;
    @Resource private TerraBootApiStoredResultsFetcher terraBootApiStoredResultsFetcher;

    /** retrieve terraform result. */
    public void retrieveTerraformResult(ServiceOrderEntity serviceOrder) {
        try {
            ReFetchResult reFetchResult =
                    terraBootApiStoredResultsFetcher.reFetchResultByOrderId(
                            serviceOrder.getOrderId());
            handleReFetchedResult(reFetchResult, serviceOrder);
        } catch (RestClientException e) {
            log.error(
                    "Failed to reFetch results of orders from terra-boot. OrderId {}. Error message"
                            + " {}.",
                    serviceOrder.getOrderId(),
                    e.getMessage());
            ServiceDeploymentEntity serviceDeployment = serviceOrder.getServiceDeploymentEntity();
            deployResultManager.saveDeploymentResultWhenErrorReceived(
                    serviceDeployment,
                    serviceOrder,
                    ErrorType.TERRA_BOOT_REQUEST_FAILED,
                    e.getMessage());
        }
    }

    /**
     * batch retrieve terraform results.
     *
     * @param serviceOrders service orders.
     */
    public void batchRetrieveTerraformResults(List<ServiceOrderEntity> serviceOrders) {
        if (CollectionUtils.isEmpty(serviceOrders)) {
            return;
        }
        Map<UUID, ServiceOrderEntity> serviceOrdersMap = new HashMap<>();
        serviceOrders.forEach(
                serviceOrder -> {
                    serviceOrdersMap.put(serviceOrder.getOrderId(), serviceOrder);
                });
        List<UUID> orderIds = new ArrayList<>(serviceOrdersMap.keySet());
        if (CollectionUtils.isEmpty(orderIds)) {
            return;
        }
        try {
            List<ReFetchResult> reFetchResults =
                    terraBootApiStoredResultsFetcher.batchReFetchResultsByOrderIds(orderIds);
            reFetchResults.forEach(
                    reFetchResult -> {
                        ServiceOrderEntity serviceOrder =
                                serviceOrdersMap.get(reFetchResult.getRequestId());
                        handleReFetchedResult(reFetchResult, serviceOrder);
                    });
        } catch (RestClientException e) {
            log.error(
                    "Failed to reFetch results of orders from terra-boot. OrderIds {}. Error"
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
                && Objects.nonNull(reFetchResult.getTerraformResult())) {
            terraformDeploymentResultCallbackManager.orderCallback(
                    reFetchResult.getRequestId(), reFetchResult.getTerraformResult());
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
