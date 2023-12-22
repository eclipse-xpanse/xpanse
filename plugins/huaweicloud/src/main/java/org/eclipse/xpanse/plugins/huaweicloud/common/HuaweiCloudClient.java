/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.common;

import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy.ERROR_CODE_INTERNAL_SERVER_ERROR;
import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy.ERROR_CODE_TOO_MANY_REQUESTS;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.region.CesRegion;
import com.huaweicloud.sdk.core.HttpListener;
import com.huaweicloud.sdk.core.SdkResponse;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.core.http.HttpConfig;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.region.EcsRegion;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HuaweiCloud Service Client.
 */
@Slf4j
@Component
public class HuaweiCloudClient extends HuaweiCloudCredentials {

    /**
     * Get HuaweiCloud CES Client.
     *
     * @param credential ICredential.
     */
    public CesClient getCesClient(ICredential credential, String regionName) {
        return CesClient.newBuilder()
                .withHttpConfig(getHttpConfig())
                .withCredential(credential)
                .withRegion(CesRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud ECS Client.
     *
     * @param credential ICredential.
     */
    public EcsClient getEcsClient(ICredential credential, String regionName) {
        return EcsClient.newBuilder()
                .withCredential(credential)
                .withRegion(EcsRegion.valueOf(regionName))
                .build();
    }


    /**
     * Match retry condition.
     *
     * @param response response
     * @param ex       exception
     * @return true if match retry condition, otherwise false
     */
    public boolean matchRetryCondition(SdkResponse response, Exception ex) {
        if (Objects.isNull(ex)) {
            return false;
        }
        if (!ServiceResponseException.class.isAssignableFrom(ex.getClass())) {
            return false;
        }
        int statusCode = ((ServiceResponseException) ex).getHttpStatusCode();
        return statusCode == ERROR_CODE_TOO_MANY_REQUESTS
                || statusCode == ERROR_CODE_INTERNAL_SERVER_ERROR;
    }


    private HttpConfig getHttpConfig() {
        HttpConfig httpConfig = HttpConfig.getDefaultHttpConfig();
        if (log.isInfoEnabled()) {
            HttpListener requestListener = HttpListener.forRequestListener(
                    listener -> log.info("> Request %s %s\n> Headers:\n%s\n> Body: %s\n",
                            listener.httpMethod(), listener.uri(),
                            getRequestHeadersString(listener),
                            listener.body().orElse("")));
            httpConfig.addHttpListener(requestListener);

            HttpListener responseListener = HttpListener.forResponseListener(
                    listener -> log.info("< Response %s %s %s\n< Headers:\n%s\n< Body: %s\n",
                            listener.httpMethod(), listener.uri(), listener.statusCode(),
                            getResponseHeadersString(listener),
                            listener.body().orElse("")));
            httpConfig.addHttpListener(responseListener);
        }
        return httpConfig;
    }

    private String getRequestHeadersString(HttpListener.RequestListener listener) {
        return listener.headers().entrySet().stream().flatMap(entry -> entry.getValue().stream()
                        .map(value -> "\t" + entry.getKey() + ": " + value))
                .collect(Collectors.joining("\n"));
    }

    private String getResponseHeadersString(HttpListener.ResponseListener listener) {
        return listener.headers().entrySet().stream().flatMap(entry -> entry.getValue().stream()
                        .map(value -> "\t" + entry.getKey() + ": " + value))
                .collect(Collectors.joining("\n"));
    }
}
