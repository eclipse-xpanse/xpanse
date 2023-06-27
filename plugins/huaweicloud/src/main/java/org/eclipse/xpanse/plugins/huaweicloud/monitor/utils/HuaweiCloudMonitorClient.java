/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.utils;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.region.CesRegion;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.springframework.stereotype.Component;

/**
 * HuaweiCloudMonitor Client Util.
 */
@Slf4j
@Component
public class HuaweiCloudMonitorClient {

    /**
     * Get HuaweiCloud Monitor Client.
     *
     * @param icredential ICredential.
     */
    public CesClient getCesClient(ICredential icredential, String regionName) {
        return CesClient.newBuilder()
                .withCredential(icredential)
                .withRegion(CesRegion.valueOf(regionName))
                .build();
    }

    /**
     * Get Credential For Huawei Monitor Client.
     *
     * @param credentialVariables object of CredentialVariables.
     */
    public ICredential getCredentialForClient(AbstractCredentialInfo credentialVariables) {
        String accessKey = null;
        String securityKey = null;
        if (CredentialType.VARIABLES.toValue().equals(credentialVariables.getType().toValue())) {
            List<CredentialVariable> variables =
                    ((CredentialVariables) credentialVariables).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (HuaweiCloudMonitorConstants.HW_ACCESS_KEY.equals(
                        credentialVariable.getName())) {
                    accessKey = credentialVariable.getValue();
                }
                if (HuaweiCloudMonitorConstants.HW_SECRET_KEY.equals(
                        credentialVariable.getName())) {
                    securityKey = credentialVariable.getValue();
                }
            }
        }
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(securityKey)) {
            log.error("Get Credential For Client error,accessKey:{},securityKey:{}", accessKey,
                    securityKey);
            throw new CredentialsNotFoundException("Get ICredential error,AK or SK is empty");
        }
        return new BasicCredentials()
                .withAk(accessKey).withSk(securityKey);
    }
}
