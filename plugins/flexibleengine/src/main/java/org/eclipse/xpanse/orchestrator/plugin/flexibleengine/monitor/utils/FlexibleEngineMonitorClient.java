/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils;

import com.cloud.apigateway.sdk.utils.Client;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * FlexibleEngineMonitor Client Util.
 */
@Slf4j
public class FlexibleEngineMonitorClient {

    private static String accessKey;
    private static String securityKey;

    public FlexibleEngineMonitorClient(String accessKey, String securityKey) {
        this.accessKey = accessKey;
        this.securityKey = securityKey;
    }

    /**
     * Build HttpRequestBase for FlexibleEngine Monitor.
     *
     * @param url     The url of the FlexibleEngine monitor metric.
     * @param headers Request headers for FlexibleEngine monitoring metrics.
     * @return
     */
    public HttpRequestBase buildRequest(String url, Map<String, String> headers) {
        HttpRequestBase requestBase = null;
        try {
            requestBase = Client.get(accessKey, securityKey, url, headers);
        } catch (Exception e) {
            log.error("Build a request for FlexibleEngine monitor metric error.", e.getMessage());
            e.printStackTrace();
        }
        return requestBase;
    }

    /**
     * Send a request of The FlexibleEngine monitoring metric
     *
     * @param request The request of the FlexibleEngine monitoring metric.
     * @return Responses to requests for FlexibleEngine monitoring metrics.
     */
    public CloseableHttpResponse send(HttpRequestBase request) {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            response = httpClient.execute(request);
        } catch (IOException e) {
            log.error("Send a request of The FlexibleEngine monitoring metric error.",
                    e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    private CloseableHttpClient getHttpClient() {
        CloseableHttpClient client = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext, NoopHostnameVerifier.INSTANCE);
            client = HttpClients.custom().setSSLSocketFactory(sslSocketFactory)
                    .build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return client;
    }
}