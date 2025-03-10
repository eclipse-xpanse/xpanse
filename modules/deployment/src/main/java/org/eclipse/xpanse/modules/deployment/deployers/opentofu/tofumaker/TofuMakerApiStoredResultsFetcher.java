/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.RetrieveOpenTofuResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.ReFetchResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to fetch api of terra-boot. */
@Slf4j
@Component
public class TofuMakerApiStoredResultsFetcher {

    @Resource private RetrieveOpenTofuResultApi retrieveOpenTofuResultApi;

    /**
     * Fetch result by service order id.
     *
     * @param orderId service order id
     * @return re-fetched result of service order
     */
    @Retryable(
            retryFor = RestClientException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public ReFetchResult reFetchResultByOrderId(UUID orderId) {
        int retryCount =
                Objects.isNull(RetrySynchronizationManager.getContext())
                        ? 0
                        : RetrySynchronizationManager.getContext().getRetryCount();
        log.info("Re-fetch result by service order id: {}. Retry count: {}", orderId, retryCount);
        return retrieveOpenTofuResultApi.getStoredTaskResultByRequestId(orderId);
    }

    /**
     * Batch re-fetch results by service order ids.
     *
     * @param orderIds service order ids
     * @return re-fetched results of service orders
     */
    @Retryable(
            retryFor = RestClientException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public List<ReFetchResult> batchReFetchResultsByOrderIds(List<UUID> orderIds) {
        int retryCount =
                Objects.isNull(RetrySynchronizationManager.getContext())
                        ? 0
                        : RetrySynchronizationManager.getContext().getRetryCount();
        log.info(
                "Batch re-fetch results by service order ids: {}. Retry count: {}",
                orderIds,
                retryCount);
        return retrieveOpenTofuResultApi.getBatchTaskResults(orderIds);
    }
}
