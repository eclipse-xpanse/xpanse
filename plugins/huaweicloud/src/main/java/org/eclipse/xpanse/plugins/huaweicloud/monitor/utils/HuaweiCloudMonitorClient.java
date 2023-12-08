/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.utils;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.region.CesRegion;
import com.huaweicloud.sdk.core.auth.ICredential;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.plugins.huaweicloud.HuaweiCloudCredentials;
import org.springframework.stereotype.Component;

/**
 * HuaweiCloudMonitor Client Util.
 */
@Slf4j
@Component
public class HuaweiCloudMonitorClient extends HuaweiCloudCredentials {

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
