/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.auth.constants;

/**
 * Defines all the environment variables that must be set for the Openstack plugin to function
 * fully.
 */
public class OpenstackCommonEnvironmentConstants {

    public static final String USERNAME = "OS_USERNAME";
    public static final String PASSWORD = "OS_PASSWORD";
    public static final String PROJECT = "OS_PROJECT_NAME";
    public static final String DOMAIN = "OS_DOMAIN_NAME";
    public static final String USER_DOMAIN = "OS_USER_DOMAIN_NAME";
    public static final String PROJECT_DOMAIN = "OS_PROJECT_DOMAIN_NAME";
    public static final String OS_AUTH_URL = "OS_AUTH_URL";
    public static final String SERVICE_PROJECT = "OS_SERVICE_PROJECT";
    public static final String SSL_VERIFICATION_DISABLED = "OS_SSL_VERIFICATION_DISABLED";
    public static final String OPENSTACK_TESTLAB_AUTH_URL = "OPENSTACK_TESTLAB_AUTH_URL";
    public static final String PLUS_SERVER_AUTH_URL = "PLUS_SERVER_AUTH_URL";
    public static final String REGIO_CLOUD_AUTH_URL = "REGIO_CLOUD_AUTH_URL";

    private OpenstackCommonEnvironmentConstants() {
        // private constructor.
    }
}
