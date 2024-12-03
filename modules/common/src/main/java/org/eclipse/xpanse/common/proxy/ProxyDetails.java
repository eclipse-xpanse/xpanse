/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.common.proxy;

import lombok.Builder;
import lombok.Data;

/**
 * Class to hold proxy information.
 */
@Data
@Builder
public class ProxyDetails {
    private String proxyUrl;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
}
