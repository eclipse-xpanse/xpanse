/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.RetrieveTerraformResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.ReFetchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to fetch api of terra-boot. */
@Slf4j
@Component
public class TerraBootApiStoredResultsFetcher {

    private final RetrieveTerraformResultApi retrieveTerraformResultApi;

    /** Constructor method. */
    @Autowired
    public TerraBootApiStoredResultsFetcher(RetrieveTerraformResultApi retrieveTerraformResultApi) {
        this.retrieveTerraformResultApi = retrieveTerraformResultApi;
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
        return retrieveTerraformResultApi.getStoredTaskResultByRequestId(orderId);
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
        return retrieveTerraformResultApi.getBatchTaskResults(orderIds);
    }
}
