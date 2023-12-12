/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine;

/**
 * Credential for FlexibleEngine Client.
 */
public record FlexibleEngineClientCredential(String accessKey,
                                             String secretKey) {
}
