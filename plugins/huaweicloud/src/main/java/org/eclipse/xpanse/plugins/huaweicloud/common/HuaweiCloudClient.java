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
import com.huaweicloud.sdk.iam.v3.region.IamRegion;
import com.huaweicloud.sdk.vpc.v2.VpcClient;
import com.huaweicloud.sdk.vpc.v2.region.VpcRegion;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.proxy.ProxyConfigurationManager;
import org.eclipse.xpanse.plugins.huaweicloud.config.HuaweiCloudPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** HuaweiCloud Service Client. */
@Slf4j
@Component
public class HuaweiCloudClient extends HuaweiCloudCredentials {

    private final ProxyConfigurationManager proxyConfigurationManager;
    private final HuaweiCloudPluginProperties huaweiCloudPluginProperties;

    @Autowired
    public HuaweiCloudClient(
            ProxyConfigurationManager proxyConfigurationManager,
            HuaweiCloudPluginProperties huaweiCloudPluginProperties) {
        this.proxyConfigurationManager = proxyConfigurationManager;
        this.huaweiCloudPluginProperties = huaweiCloudPluginProperties;
    }

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
     * @param regionName region.
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

    private HttpConfig getHttpConfig() {
        HttpConfig httpConfig = HttpConfig.getDefaultHttpConfig();
        if (huaweiCloudPluginProperties.getEnableSdkHttpDebugLogs()) {
            HttpListener requestListener = HttpListener.forRequestListener(this::outputRequestInfo);
            httpConfig.addHttpListener(requestListener);

            HttpListener responseListener =
                    HttpListener.forResponseListener(this::outputResponseInfo);
            httpConfig.addHttpListener(responseListener);
        }
        if (proxyConfigurationManager.getHttpsProxyDetails() != null) {
            httpConfig.setProxyHost(
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyHost());
            httpConfig.setProxyPort(
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyPort());
            httpConfig.setProxyUsername(
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyUsername());
            httpConfig.setProxyPassword(
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyPassword());
        }
        return httpConfig;
    }

    private void outputRequestInfo(HttpListener.RequestListener listener) {
        String requestInfo =
                "> Request "
                        + listener.httpMethod()
                        + " "
                        + listener.uri()
                        + System.lineSeparator()
                        + "> Headers:"
                        + System.lineSeparator()
                        + getRequestHeadersString(listener)
                        + System.lineSeparator()
                        + "> Body:"
                        + listener.body().orElse("")
                        + System.lineSeparator();
        log.info(requestInfo);
    }

    private void outputResponseInfo(HttpListener.ResponseListener listener) {
        String responseInfo =
                "< Response "
                        + listener.httpMethod()
                        + " "
                        + listener.uri()
                        + " "
                        + listener.statusCode()
                        + System.lineSeparator()
                        + "< Headers:"
                        + System.lineSeparator()
                        + getResponseHeadersString(listener)
                        + System.lineSeparator()
                        + "< Body:"
                        + listener.body().orElse("")
                        + System.lineSeparator();
        log.info(responseInfo);
    }

    private String getRequestHeadersString(HttpListener.RequestListener listener) {
        return listener.headers().entrySet().stream()
                .flatMap(
                        entry ->
                                entry.getValue().stream()
                                        .map(value -> "\t" + entry.getKey() + ": " + value))
                .collect(Collectors.joining("\n"));
    }

    private String getResponseHeadersString(HttpListener.ResponseListener listener) {
        return listener.headers().entrySet().stream()
                .flatMap(
                        entry ->
                                entry.getValue().stream()
                                        .map(value -> "\t" + entry.getKey() + ": " + value))
                .collect(Collectors.joining("\n"));
    }
}
