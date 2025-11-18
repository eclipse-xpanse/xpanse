/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.RetrieveOpenTofuResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.ReFetchResult;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to fetch api of terra-boot. */
@Slf4j
@Component
public class TofuMakerApiStoredResultsFetcher {

    private final RetrieveOpenTofuResultApi retrieveOpenTofuResultApi;

    public TofuMakerApiStoredResultsFetcher(RetrieveOpenTofuResultApi retrieveOpenTofuResultApi) {
        this.retrieveOpenTofuResultApi = retrieveOpenTofuResultApi;
    }

    /**
     * Fetch result by service order id.
     *
     * @param orderId service order id
     * @return re-fetched result of service order
     */
    @Retryable(
            retryFor = RestClientException.class,
            maxAttemptsExpression = "${xpanse.http-client-request.retry-max-attempts}",
            backoff =
                    @Backoff(delayExpression = "${xpanse.http-client-request.delay-milliseconds}"))
    public ReFetchResult reFetchResultByOrderId(UUID orderId) {
        RetryContext retryContext = RetrySynchronizationManager.getContext();
        int retryCount = Objects.isNull(retryContext) ? 0 : retryContext.getRetryCount();
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
            maxAttemptsExpression = "${xpanse.http-client-request.retry-max-attempts}",
            backoff =
                    @Backoff(delayExpression = "${xpanse.http-client-request.delay-milliseconds}"))
    public List<ReFetchResult> batchReFetchResultsByOrderIds(List<UUID> orderIds) {
        RetryContext retryContext = RetrySynchronizationManager.getContext();
        int retryCount = Objects.isNull(retryContext) ? 0 : retryContext.getRetryCount();
        log.info(
                "Batch re-fetch results by service order ids: {}. Retry count: {}",
                orderIds,
                retryCount);
        return retrieveOpenTofuResultApi.getBatchTaskResults(orderIds);
    }
}
