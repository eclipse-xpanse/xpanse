/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor.models;

/**
 * Credential for FlexibleEngine Monitor Client.
 */
public record FlexibleEngineMonitorClientCredential(String accessKey,
                                                    String secretKey) {
}
