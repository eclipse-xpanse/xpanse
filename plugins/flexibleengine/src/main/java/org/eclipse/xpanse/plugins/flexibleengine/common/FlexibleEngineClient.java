/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.common;

import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.CES_ENDPOINT_PREFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.ECS_ENDPOINT_PREFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.EIP_ENDPOINT_PREFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.ENDPOINT_SUFFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.EVS_ENDPOINT_PREFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.IAM_ENDPOINT_PREFIX;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.PROTOCOL_HTTPS;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants.VPC_ENDPOINT_PREFIX;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.core.HcClient;
import com.huaweicloud.sdk.core.HttpListener;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.http.HttpConfig;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.eip.v2.EipClient;
import com.huaweicloud.sdk.evs.v2.EvsClient;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsRequest;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.vpc.v2.VpcClient;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.plugins.flexibleengine.config.FlexibleEnginePluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/** FlexibleEngine Service Client. */
@Slf4j
@RefreshScope
@Component
public class FlexibleEngineClient extends FlexibleEngineCredentials {

    private final FlexibleEnginePluginProperties flexibleEnginePluginProperties;

    private final FlexibleEngineRetryStrategy flexibleEngineRetryStrategy;

    @Autowired
    public FlexibleEngineClient(
            FlexibleEnginePluginProperties flexibleEnginePluginProperties,
            FlexibleEngineRetryStrategy flexibleEngineRetryStrategy) {
        this.flexibleEnginePluginProperties = flexibleEnginePluginProperties;
        this.flexibleEngineRetryStrategy = flexibleEngineRetryStrategy;
    }

    /**
     * Get client for service ECS.
     *
     * @param credential credential
     * @param regionName region name
     * @return client for service ECS.
     */
    public EcsClient getEcsClient(ICredential credential, String regionName) {
        HcClient hcClient = getHcClient(credential, ECS_ENDPOINT_PREFIX, regionName);
        return new EcsClient(hcClient);
    }

    /**
     * Get client for service CES.
     *
     * @param credential credential
     * @param regionName region name
     * @return client for service CES.
     */
    public CesClient getCesClient(ICredential credential, String regionName) {
        HcClient hcClient = getHcClient(credential, CES_ENDPOINT_PREFIX, regionName);
        return new CesClient(hcClient);
    }

    /**
     * Get client for service VPC.
     *
     * @param credential credential
     * @param regionName region name
     * @return client for service VPC.
     */
    public VpcClient getVpcClient(ICredential credential, String regionName) {
        HcClient hcClient = getHcClient(credential, VPC_ENDPOINT_PREFIX, regionName);
        return new VpcClient(hcClient);
    }

    /**
     * Get client for service EIP.
     *
     * @param credential credential
     * @param regionName region name
     * @return client for service EIP.
     */
    public EipClient getEipClient(ICredential credential, String regionName) {
        HcClient hcClient = getHcClient(credential, EIP_ENDPOINT_PREFIX, regionName);
        return new EipClient(hcClient);
    }

    /**
     * Get client for service EVS.
     *
     * @param credential credential
     * @param regionName region name
     * @return client for service EVS.
     */
    public EvsClient getEvsClient(ICredential credential, String regionName) {
        HcClient hcClient = getHcClient(credential, EVS_ENDPOINT_PREFIX, regionName);
        return new EvsClient(hcClient);
    }

    private HcClient getHcClient(ICredential credential, String servicePrefix, String regionName) {
        String endpoint = PROTOCOL_HTTPS + servicePrefix + regionName + ENDPOINT_SUFFIX;
        HcClient hcClient = new HcClient(getHttpConfig());
        hcClient.withCredential(getCredentialWithProjectId(credential, regionName));
        hcClient.withEndpoints(List.of(endpoint));
        return hcClient;
    }

    private ICredential getCredentialWithProjectId(ICredential credential, String regionName) {
        String projectId = getProjectId(credential, regionName);
        if (Objects.nonNull(credential)
                && StringUtils.isNotBlank(projectId)
                && credential instanceof BasicCredentials basicCredentials) {
            String accessKey = basicCredentials.getAk();
            String securityKey = basicCredentials.getSk();
            return new BasicCredentials()
                    .withAk(accessKey)
                    .withSk(securityKey)
                    .withProjectId(projectId);
        }
        return credential;
    }

    private String getProjectId(ICredential credential, String regionName) {
        String projectId = null;
        try {
            String endpoint = PROTOCOL_HTTPS + IAM_ENDPOINT_PREFIX + regionName + ENDPOINT_SUFFIX;
            HcClient hcClient = new HcClient(HttpConfig.getDefaultHttpConfig());
            hcClient.withCredential(credential);
            hcClient.withEndpoints(List.of(endpoint));
            IamClient iamClient = new IamClient(hcClient);
            KeystoneListProjectsRequest listProjectsRequest = new KeystoneListProjectsRequest();
            listProjectsRequest.setName(regionName);
            KeystoneListProjectsResponse response =
                    iamClient
                            .keystoneListProjectsInvoker(listProjectsRequest)
                            .retryTimes(flexibleEngineRetryStrategy.getRetryMaxAttempts())
                            .retryCondition(flexibleEngineRetryStrategy::matchRetryCondition)
                            .backoffStrategy(flexibleEngineRetryStrategy)
                            .invoke();
            if (Objects.nonNull(response) && CollectionUtils.isNotEmpty(response.getProjects())) {
                projectId = response.getProjects().getFirst().getId();
            }
            return projectId;
        } catch (Exception e) {
            log.error("Get project id with region {} failed.", regionName);
            flexibleEngineRetryStrategy.handleAuthExceptionForSpringRetry(e);
            throw new ClientApiCallFailedException(e.getMessage());
        }
    }

    private HttpConfig getHttpConfig() {
        HttpConfig httpConfig = HttpConfig.getDefaultHttpConfig();
        if (flexibleEnginePluginProperties.getEnableSdkHttpDebugLogs()) {
            HttpListener requestListener = HttpListener.forRequestListener(this::outputRequestInfo);
            httpConfig.addHttpListener(requestListener);

            HttpListener responseListener =
                    HttpListener.forResponseListener(this::outputResponseInfo);
            httpConfig.addHttpListener(responseListener);
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
