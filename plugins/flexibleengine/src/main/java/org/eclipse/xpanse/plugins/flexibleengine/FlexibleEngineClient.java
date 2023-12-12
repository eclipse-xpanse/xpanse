/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine;

import com.cloud.apigateway.sdk.utils.Client;
import java.util.List;
import java.util.Map;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.plugins.flexibleengine.models.constant.FlexibleEngineConstants;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * FlexibleEngine Monitor Client.
 */
@Component
public class FlexibleEngineClient {

    private FlexibleEngineClientCredential getCredentialForClient(
            AbstractCredentialInfo credential) {
        String accessKey = null;
        String securityKey = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = ((CredentialVariables) credential).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (FlexibleEngineConstants.OS_ACCESS_KEY.equals(
                        credentialVariable.getName())) {
                    accessKey = credentialVariable.getValue();
                }
                if (FlexibleEngineConstants.OS_SECRET_KEY.equals(
                        credentialVariable.getName())) {
                    securityKey = credentialVariable.getValue();
                }
            }
        }
        return new FlexibleEngineClientCredential(accessKey, securityKey);
    }

    /**
     * Get HttpRequestBase of the Get request of the FlexibleEngine API.
     *
     * @param credential The credential of the service.
     * @param url        The request url of the FlexibleEngine API.
     * @return Returns HttpRequestBase
     */
    public HttpRequestBase buildGetRequest(AbstractCredentialInfo credential, String url)
            throws Exception {

        FlexibleEngineClientCredential clientCredential = getCredentialForClient(credential);
        return Client.get(clientCredential.accessKey(), clientCredential.secretKey(), url,
                Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE));
    }

    /**
     * Get HttpRequestBase of the Get request of the FlexibleEngine API.
     *
     * @param credential The credential of the service.
     * @param url        The request url of the FlexibleEngine API.
     * @return Returns HttpRequestBase
     */
    public HttpRequestBase buildPostRequest(AbstractCredentialInfo credential, String url,
            String postBody) throws Exception {
        FlexibleEngineClientCredential clientCredential = getCredentialForClient(credential);
        return Client.post(clientCredential.accessKey(), clientCredential.secretKey(), url,
                Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE), postBody);
    }
}
