/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.manage;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.region.EcsRegion;
import org.eclipse.xpanse.plugins.huaweicloud.HuaweiCloudCredentials;
import org.springframework.stereotype.Component;

/**
 * HuaweiCloud Ecs Client Util.
 */
@Component
public class HuaweiCloudManagerClient extends HuaweiCloudCredentials {

    /**
     * Get HuaweiCloud Monitor Client.
     *
     * @param icredential ICredential.
     */
    public EcsClient getEcsClient(ICredential icredential, String regionName) {
        return EcsClient.newBuilder()
                .withCredential(icredential)
                .withRegion(EcsRegion.valueOf(regionName))
                .build();
    }
}