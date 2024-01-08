/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.scs.common.constants;

/**
 * Defines all the environment variables that must be set for the SCS plugin to function
 * fully.
 */
public class ScsEnvironmentConstants {

    public static final String USERNAME = "OS_USERNAME";
    public static final String PASSWORD = "OS_PASSWORD";
    public static final String PROJECT = "OS_PROJECT_NAME";
    public static final String DOMAIN = "OS_DOMAIN_NAME";
    public static final String AUTH_URL = "OS_AUTH_URL";

    private ScsEnvironmentConstants() {
        // private constructor.
    }
}
