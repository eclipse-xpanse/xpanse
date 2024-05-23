/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.common;

import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy.ERROR_CODE_INTERNAL_SERVER_ERROR;
import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy.ERROR_CODE_TOO_MANY_REQUESTS;

import com.huaweicloud.sdk.bss.v2.BssClient;
import com.huaweicloud.sdk.bss.v2.region.BssRegion;
import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.region.CesRegion;
import com.huaweicloud.sdk.core.HttpListener;
import com.huaweicloud.sdk.core.SdkResponse;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.core.http.HttpConfig;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.region.EcsRegion;
import com.huaweicloud.sdk.eip.v2.EipClient;
import com.huaweicloud.sdk.eip.v2.region.EipRegion;
import com.huaweicloud.sdk.evs.v2.EvsClient;
import com.huaweicloud.sdk.evs.v2.region.EvsRegion;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.region.IamRegion;
import com.huaweicloud.sdk.vpc.v2.VpcClient;
import com.huaweicloud.sdk.vpc.v2.region.VpcRegion;
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
     * @param regionName region.
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
     * @param regionName region.
     */
    public EcsClient getEcsClient(ICredential credential, String regionName) {
        return EcsClient.newBuilder()
                .withCredential(credential)
                .withRegion(EcsRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud Vpc Client.
     *
     * @param credential ICredential.
     * @param regionName region.
     */
    public VpcClient getVpcClient(ICredential credential, String regionName) {
        return VpcClient.newBuilder()
                .withCredential(credential)
                .withRegion(VpcRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud Eip Client.
     *
     * @param basicCredential ICredential
     * @param regionName      region.
     */
    public EipClient getEipClient(ICredential basicCredential, String regionName) {
        return EipClient.newBuilder()
                .withCredential(basicCredential)
                .withRegion(EipRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud Evs Client.
     *
     * @param basicCredential ICredential
     * @param regionName      region.
     */
    public EvsClient getEvsClient(ICredential basicCredential, String regionName) {
        return EvsClient.newBuilder()
                .withCredential(basicCredential)
                .withRegion(EvsRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud Iam Client.
     *
     * @param globalCredential ICredential
     * @param regionName       region.
     */
    public IamClient getIamClient(ICredential globalCredential, String regionName) {
        return IamClient.newBuilder()
                .withCredential(globalCredential)
                .withRegion(IamRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud Bss Client.
     *
     * @param globalCredential ICredential
     */
    public BssClient getBssClient(ICredential globalCredential) {
        return BssClient.newBuilder()
                .withCredential(globalCredential)
                .withRegion(BssRegion.CN_NORTH_1)
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
