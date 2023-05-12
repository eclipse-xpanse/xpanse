/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.monitor.utils;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.region.CesRegion;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * HuaweiCloudMonitor Client Util.
 */
@Slf4j
@Component
public class HuaweiCloudMonitorClient {

    /**
     * Get Huawei Monitor ICredential by IAM.
     *
     * @param accessKey   The access key for HuaweiCloud.
     * @param securityKey The security key for HuaweiCloud.
     */
    public ICredential getIcredentialWithAkSk(String accessKey, String securityKey) {
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(securityKey)) {
            log.error("Get ICredential error,accessKey:{},securityKey:{}", accessKey, securityKey);
            throw new IllegalArgumentException("Get ICredential error,AK or SK is empty");
        }
        return new BasicCredentials()
                .withAk(accessKey).withSk(securityKey);
    }

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
}
