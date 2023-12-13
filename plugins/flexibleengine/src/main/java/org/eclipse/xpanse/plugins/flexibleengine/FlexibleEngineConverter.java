/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.plugins.flexibleengine.models.constant.FlexibleEngineConstants;
import org.springframework.stereotype.Component;

/**
 * FlexibleEngine Resource Converter.
 */
@Slf4j
@Component
public class FlexibleEngineConverter {

    /**
     * Get url to query project info.
     *
     * @param region The region of resource.
     * @return Returns query url.
     */
    public StringBuilder buildProjectQueryUrl(String region) {
        return new StringBuilder(FlexibleEngineConstants.PROTOCOL_HTTPS)
                .append(FlexibleEngineConstants.IAM_ENDPOINT_PREFIX)
                .append(region)
                .append(FlexibleEngineConstants.ENDPOINT_SUFFIX).append("/")
                .append(FlexibleEngineConstants.IAM_API_VERSION).append("/")
                .append(FlexibleEngineConstants.PROJECTS_PATH).append("?")
                .append("name=").append(region);
    }
}
