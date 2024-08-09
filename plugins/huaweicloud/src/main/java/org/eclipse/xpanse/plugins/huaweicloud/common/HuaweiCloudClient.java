/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.common;

import com.huaweicloud.sdk.bss.v2.BssClient;
import com.huaweicloud.sdk.bss.v2.region.BssRegion;
import com.huaweicloud.sdk.bssintl.v2.BssintlClient;
import com.huaweicloud.sdk.bssintl.v2.region.BssintlRegion;
import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.region.CesRegion;
import com.huaweicloud.sdk.core.HttpListener;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.http.HttpConfig;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.region.EcsRegion;
import com.huaweicloud.sdk.eip.v2.EipClient;
import com.huaweicloud.sdk.eip.v2.region.EipRegion;
import com.huaweicloud.sdk.evs.v2.EvsClient;
import com.huaweicloud.sdk.evs.v2.region.EvsRegion;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsRequest;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.iam.v3.region.IamRegion;
import com.huaweicloud.sdk.vpc.v2.VpcClient;
import com.huaweicloud.sdk.vpc.v2.region.VpcRegion;
import jakarta.annotation.Resource;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * HuaweiCloud Service Client.
 */
@Slf4j
@Component
public class HuaweiCloudClient extends HuaweiCloudCredentials {

    @Value("${huaweicloud.sdk.enable.http.debug.logs:false}")
    private boolean sdkHttpDebugLogsEnabled;

    @Resource
    private HuaweiCloudRetryStrategy huaweiCloudRetryStrategy;

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
                .withHttpConfig(getHttpConfig())
                .withCredential(credential)
                .withRegion(VpcRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud Eip Client.
     *
     * @param credential ICredential
     * @param regionName region.
     */
    public EipClient getEipClient(ICredential credential, String regionName) {
        return EipClient.newBuilder()
                .withHttpConfig(getHttpConfig())
                .withCredential(credential)
                .withRegion(EipRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get HuaweiCloud Evs Client.
     *
     * @param credential ICredential
     * @param regionName region.
     */
    public EvsClient getEvsClient(ICredential credential, String regionName) {
        return EvsClient.newBuilder()
                .withHttpConfig(getHttpConfig())
                .withCredential(credential)
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
                .withHttpConfig(getHttpConfig())
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
                .withHttpConfig(getHttpConfig())
                .withCredential(globalCredential)
                // The fixed BssRegion maps the endpoint 'https://bss.myhuaweicloud.com'
                .withRegion(BssRegion.CN_NORTH_1)
                .build();
    }

    /**
     * Get HuaweiCloud Bss intl Client.
     *
     * @param globalCredential ICredential
     */
    public BssintlClient getBssintlClient(ICredential globalCredential) {
        return BssintlClient.newBuilder()
                .withHttpConfig(getHttpConfig())
                .withCredential(globalCredential)
                // The fixed BssintlRegion maps the endpoint 'https://bss-intl.myhuaweicloud.com'.
                .withRegion(BssintlRegion.AP_SOUTHEAST_1)
                .build();
    }


    /**
     * Get projectId with region.
     *
     * @param globalCredential ICredential
     * @param regionName       region name.
     * @return projectId.
     */
    public String getProjectId(ICredential globalCredential, String regionName) {
        String projectId = null;
        try {
            IamClient iamClient = getIamClient(globalCredential, regionName);
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

    private HttpConfig getHttpConfig() {
        HttpConfig httpConfig = HttpConfig.getDefaultHttpConfig();
        if (sdkHttpDebugLogsEnabled) {
            HttpListener requestListener =
                    HttpListener.forRequestListener(this::outputRequestInfo);
            httpConfig.addHttpListener(requestListener);

            HttpListener responseListener =
                    HttpListener.forResponseListener(this::outputResponseInfo);
            httpConfig.addHttpListener(responseListener);
        }
        return httpConfig;
    }

    private void outputRequestInfo(HttpListener.RequestListener listener) {
        String requestInfo = "> Request " + listener.httpMethod() + " " + listener.uri()
                + System.lineSeparator()
                + "> Headers:" + System.lineSeparator() + getRequestHeadersString(listener)
                + System.lineSeparator() + "> Body:" + listener.body().orElse("")
                + System.lineSeparator();
        log.info(requestInfo);
    }

    private void outputResponseInfo(HttpListener.ResponseListener listener) {
        String responseInfo = "< Response " + listener.httpMethod() + " " + listener.uri() + " "
                + listener.statusCode() + System.lineSeparator()
                + "< Headers:" + System.lineSeparator() + getResponseHeadersString(listener)
                + System.lineSeparator() + "< Body:" + listener.body().orElse("")
                + System.lineSeparator();
        log.info(responseInfo);
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
