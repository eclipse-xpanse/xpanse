/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import java.util.List;

/**
 * Class holds all static values of property names in the xpanse's global schema. Properties with
 * names will be either updated, replaced or removed when the controller API is generated.
 */
public class XpanseGlobalNames {

    public static final List<String> FIELDS_TO_BE_REMOVED =
            List.of(
                    "serviceName",
                    "category",
                    "objectType",
                    "actionName",
                    "serviceTemplateId",
                    "userId");

    public static final String SERVICE_CONFIGURATION_TYPE_NAME = "ServiceConfigurationDetails";
    public static final String SERVICE_ORDER_DETAILS = "ServiceOrderDetails";

    public static final String SERVICE_INPUTS_PROPERTY_NAME = "serviceRequestProperties";
    public static final String CONFIGURATION_VALUES_PROPERTY_NAME = "configuration";
    public static final String ACTION_INPUTS_PROPERTY_NAME = "actionParameters";
    public static final String SERVICE_OBJECT_INPUTS_PROPERTY_NAME = "serviceObjectParameters";

    public static final String REGION_PROPERTY_NAME = "region";
    public static final String SERVICE_HOSTING_TYPE_PROPERTY_NAME = "serviceHostingType";
    public static final String CSP_TYPE_PROPERTY_NAME = "csp";
    public static final String SERVICE_VERSION_PROPERTY_NAME = "version";
}
