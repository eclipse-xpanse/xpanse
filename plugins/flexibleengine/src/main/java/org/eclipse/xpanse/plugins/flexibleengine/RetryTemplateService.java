/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine;

import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.internal.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate with Retry.
 */
@Slf4j
@Component
@EnableRetry
public class RetryTemplateService {

    private static final int RETRY_TIMES = 5;
    private static final long DELAY_MILLISECONDS = 2000L;
    private static final double DELAY_MULTIPLIER = 1.5D;

    @Resource
    @Qualifier("flexibleEngineRestTemplate")
    private RestTemplate restTemplate;

    /**
     * Query project info using RestTemplate with Spring-Retry.
     *
     * @param httpRequestBase httpRequestBase
     * @return Project
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public KeystoneListProjectsResponse queryProjectInfo(HttpRequestBase httpRequestBase) {
        HttpEntity<String> httpEntity =
                new HttpEntity<>("parameters", getHttpHeaders(httpRequestBase));
        ResponseEntity<KeystoneListProjectsResponse> response =
                restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.GET, httpEntity,
                        KeystoneListProjectsResponse.class);
        if (Objects.nonNull(response.getBody())) {
            return response.getBody();
        }
        throw new RestClientException("query project info result is null.");
    }


    /**
     * Query metric data using RestTemplate with Spring-Retry.
     *
     * @param httpRequestBase httpRequestBase
     * @return ShowMetricDataResponse
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public ShowMetricDataResponse queryMetricData(HttpRequestBase httpRequestBase)
            throws RestClientException {
        HttpEntity<String> httpEntity =
                new HttpEntity<>("parameters", getHttpHeaders(httpRequestBase));
        ResponseEntity<ShowMetricDataResponse> responseEntity =
                restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.GET, httpEntity,
                        ShowMetricDataResponse.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity.getBody();
        }
        throw new RestClientException("query metric data failed.");
    }

    /**
     * Query metric item list using RestTemplate with Spring-Retry.
     *
     * @param httpRequestBase httpRequestBase
     * @return ShowMetricDataResponse
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public ListMetricsResponse queryMetricItemList(HttpRequestBase httpRequestBase) {
        HttpEntity<String> httpEntity =
                new HttpEntity<>("parameters", getHttpHeaders(httpRequestBase));
        ResponseEntity<ListMetricsResponse> responseEntity =
                restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.GET, httpEntity,
                        ListMetricsResponse.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity.getBody();
        }
        throw new RestClientException("query metric item list failed.");
    }

    /**
     * Batch query metric data using RestTemplate with Spring-Retry.
     *
     * @param httpRequestBase httpRequestBase
     * @param requestBody     requestBody
     * @return BatchListMetricDataResponse
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public BatchListMetricDataResponse batchQueryMetricData(HttpRequestBase httpRequestBase,
            String requestBody) {
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody,
                getHttpHeaders(httpRequestBase));
        ResponseEntity<BatchListMetricDataResponse> responseEntity =
                restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.POST, httpEntity,
                        BatchListMetricDataResponse.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity.getBody();
        }
        throw new RestClientException("batch query metric data failed.");
    }

    /**
     * Start service.
     *
     * @param httpRequestBase httpRequestBase
     * @param requestBody     requestBody
     * @return BatchStartServersResponse
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public ResponseEntity<BatchStartServersResponse> startService(HttpRequestBase httpRequestBase,
            String requestBody) {
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody,
                getHttpHeaders(httpRequestBase));
        return restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.POST, httpEntity,
                BatchStartServersResponse.class);
    }

    /**
     * Stop service.
     *
     * @param httpRequestBase httpRequestBase
     * @param requestBody     requestBody
     * @return BatchStopServersResponse
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public ResponseEntity<BatchStopServersResponse> stopService(HttpRequestBase httpRequestBase,
            String requestBody) {
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody,
                getHttpHeaders(httpRequestBase));
        return restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.POST, httpEntity,
                BatchStopServersResponse.class);
    }

    /**
     * Restart service.
     *
     * @param httpRequestBase httpRequestBase
     * @param requestBody     requestBody
     * @return BatchRebootServersResponse
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public ResponseEntity<BatchRebootServersResponse> restartService(
            HttpRequestBase httpRequestBase,
            String requestBody) {
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody,
                getHttpHeaders(httpRequestBase));
        return restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.POST, httpEntity,
                BatchRebootServersResponse.class);
    }

    /**
     * Query JOB execution status.
     *
     * @param httpRequestBase httpRequestBase
     * @return ShowJobResponse
     */
    @Retryable(retryFor = RestClientException.class, maxAttempts = RETRY_TIMES,
            listeners = "restTemplateRetryListener",
            backoff = @Backoff(delay = DELAY_MILLISECONDS, multiplier = DELAY_MULTIPLIER))
    public ResponseEntity<ShowJobResponse> checkEcsExecResultByJobId(
            HttpRequestBase httpRequestBase) {
        HttpEntity<String> httpEntity =
                new HttpEntity<>("parameters", getHttpHeaders(httpRequestBase));
        return restTemplate.exchange(httpRequestBase.getURI(), HttpMethod.GET, httpEntity,
                ShowJobResponse.class);
    }

    private HttpHeaders getHttpHeaders(HttpRequestBase httpRequestBase) {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> headersMap = Arrays.stream(httpRequestBase.getAllHeaders())
                .collect(Collectors.toMap(Header::getName, Header::getValue));
        headers.setAll(headersMap);
        return headers;
    }

    /**
     * Handling exception after the get request retried max times.
     *
     * @param e               RestClientException
     * @param httpRequestBase httpRequestBase
     * @return null
     */
    @Recover
    public Object recoverGetRequest(RestClientException e, HttpRequestBase httpRequestBase) {
        log.error("RestTemplate get request[Url:{}] still failed after retried {} times.",
                httpRequestBase.getURI(), RETRY_TIMES, e);
        return null;
    }

    /**
     * Handling exception after the post request retried max times.
     *
     * @param e               RestClientException
     * @param httpRequestBase httpRequestBase
     * @return null
     */
    @Recover
    public Object recoverPostRequest(RestClientException e, HttpRequestBase httpRequestBase,
            String requestBody) {
        log.error("RestTemplate post request[Url:{},Body:{}] still failed after retried {} times.",
                httpRequestBase.getURI(), RETRY_TIMES, requestBody, e);
        return null;
    }

    /**
     * RetryLister Bean.
     */
    @Bean("restTemplateRetryListener")
    public RetryListener retryListener() {
        return new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context,
                    RetryCallback<T, E> callback) {
                log.error("Retry open context:{}", context);
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context,
                    RetryCallback<T, E> callback,
                    Throwable throwable) {
                log.error("Retry close context:{}", context);
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext context,
                    RetryCallback<T, E> callback,
                    Throwable throwable) {
                log.error("Retry onError context:{}, error:{}.", context, throwable.getMessage());
            }
        };
    }
}
