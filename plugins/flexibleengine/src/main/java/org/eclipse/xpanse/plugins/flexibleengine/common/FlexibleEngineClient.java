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
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.DEFAULT_DELAY_MILLIS;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.DEFAULT_RETRY_TIMES;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.ERROR_CODE_INTERNAL_SERVER_ERROR;
import static org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineRetryStrategy.ERROR_CODE_TOO_MANY_REQUESTS;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.core.HcClient;
import com.huaweicloud.sdk.core.HttpListener;
import com.huaweicloud.sdk.core.SdkResponse;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
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
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.springframework.stereotype.Component;

/**
 * FlexibleEngine Service Client.
 */
@Slf4j
@Component
public class FlexibleEngineClient extends FlexibleEngineCredentials {

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

    private HcClient getHcClient(ICredential credential, String servicePrefix, String regionName) {
        String endpoint = PROTOCOL_HTTPS + servicePrefix + regionName + ENDPOINT_SUFFIX;
        HcClient hcClient = new HcClient(getHttpConfig());
        hcClient.withCredential(getCredentialWithProjectId(credential, regionName));
        hcClient.withEndpoints(List.of(endpoint));
        return hcClient;
    }

    private ICredential getCredentialWithProjectId(ICredential credential, String regionName) {
        String projectId = getProjectId(credential, regionName);
        if (Objects.nonNull(credential) && StringUtils.isNotBlank(projectId)
                && credential instanceof BasicCredentials basicCredentials) {
            String accessKey = basicCredentials.getAk();
            String securityKey = basicCredentials.getSk();
            return new BasicCredentials().withAk(accessKey).withSk(securityKey)
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
                    iamClient.keystoneListProjectsInvoker(listProjectsRequest)
                            .retryTimes(DEFAULT_RETRY_TIMES)
                            .retryCondition(this::matchRetryCondition)
                            .backoffStrategy(new FlexibleEngineRetryStrategy(DEFAULT_DELAY_MILLIS))
                            .invoke();
            if (Objects.nonNull(response) && CollectionUtils.isNotEmpty(response.getProjects())) {
                projectId = response.getProjects().get(0).getId();
            }
        } catch (RuntimeException e) {
            String errorMsg =
                    String.format("Query project id by region name: %s failed.", regionName);
            throw new ClientApiCallFailedException(errorMsg);
        }
        if (StringUtils.isNotBlank(projectId)) {
            log.info("Query project id:{} by region name:{} success.", projectId, regionName);
            return projectId;
        }
        String errorMsg = String.format("Query project id by region name: %s failed.", regionName);
        throw new ClientApiCallFailedException(errorMsg);
    }

    private HttpConfig getHttpConfig() {
        HttpConfig httpConfig = HttpConfig.getDefaultHttpConfig();
        if (log.isInfoEnabled()) {
            HttpListener requestListener = HttpListener.forRequestListener(
                    listener -> log.info("> Request %s %s\n> Headers:\n%s\n> Body: %s\n",
                            listener.httpMethod(), listener.uri(),
                            getRequestHeadersString(listener), listener.body().orElse("")));
            httpConfig.addHttpListener(requestListener);

            HttpListener responseListener = HttpListener.forResponseListener(
                    listener -> log.info("< Response %s %s %s\n< Headers:\n%s\n< Body: %s\n",
                            listener.httpMethod(), listener.uri(), listener.statusCode(),
                            getResponseHeadersString(listener), listener.body().orElse("")));
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
