/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

/**
 * class defines static variables for the extension variables. These variables will be injected into
 * the service controller's OpenAPI document.
 */
public class Extensions {

    // used for filtering out objects in the controller.
    public static final String X_OBJECT_NAME = "x-object-Name";
    public static final String X_ACTION_NAME = "x-Action-name";
    public static final String X_GLOBAL_SERVICE_MAPPINGS = "x-global-service-mappings";
    public static final String X_READ_DOMAIN = "x-read-domain";
}
