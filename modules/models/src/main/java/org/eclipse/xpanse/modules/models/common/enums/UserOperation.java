/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/** Defines operations for service template. */
public enum UserOperation {
    // Operations for service templates.
    REGISTER_SERVICE_TEMPLATE("register the service template"),
    UPDATE_SERVICE_TEMPLATE("update the service template"),
    UNPUBLISH_SERVICE_TEMPLATE("unpublish the service template"),
    REPUBLISH_SERVICE_TEMPLATE("republish the service template"),
    DELETE_SERVICE_TEMPLATE("delete the service template"),
    VIEW_DETAILS_OF_SERVICE_TEMPLATE("view details of service template"),

    // Operations for requests of service template.
    VIEW_REQUEST_HISTORY_OF_SERVICE_TEMPLATE("view request history of the service template"),
    VIEW_REQUEST_DETAILS_OF_SERVICE_TEMPLATE("view request details of the service template"),
    CANCEL_REQUEST_OF_SERVICE_TEMPLATE("cancel request of the service template"),
    REVIEW_REQUEST_OF_SERVICE_TEMPLATE("review request of the service template"),

    // Operations for service policies.
    CREATE_POLICY_OF_SERVICE_TEMPLATE("create policy of the service template"),
    UPDATE_POLICY_OF_SERVICE_TEMPLATE("update policy of the service template"),
    DELETE_POLICY_OF_SERVICE_TEMPLATE("delete policy of the service template"),
    VIEW_POLICY_DETAILS_OF_SERVICE_TEMPLATE("view policy details of the service template"),
    VIEW_POLICIES_OF_SERVICE_TEMPLATE("review policies of the service template"),

    // Operations for policies of users.
    VIEW_USER_POLICIES("review user policies"),
    CREATE_USER_POLICY("create user policy"),
    UPDATE_USER_POLICY("update user policy"),
    DELETE_USER_POLICY("delete user policy"),
    VIEW_DETAILS_OF_USER_POLICY("view details of user policy"),

    // Operations for services.
    RDEPLOY_SERVICE("deploy the service"),
    REDEPLOY_SERVICE("redeploy the service"),
    MODIFY_SERVICE("modify the service"),
    RECREATE_SERVICE("recreate the service"),
    PORT_SERVICE("port the service"),
    DESTROY_SERVICE("delete the service"),
    PURGE_SERVICE("purge the service"),
    CREATE_ACTIONS_OF_SERVICE("create actions of the service"),
    VIEW_DETAILS_OF_SERVICE("view details of the service"),
    VIEW_RESOURCES_OF_SERVICE("view resources of the service"),
    VIEW_METRICS_OF_SERVICE("view metrics of the service"),
    VIEW_CONFIGURATIONS_OF_SERVICE("view configurations of the service"),
    CHANGE_SERVICE_STATE("change the state of the service"),
    CHANGE_SERVICE_CONFIGURATION("change the configuration of the service"),
    CHANGE_SERVICE_LOCK_CONFIGURATION("change the lock configuration of the service"),

    // Operations for service orders.
    VIEW_ORDER_DETAILS_OF_SERVICE("view order details of the service"),
    VIEW_ORDERS_OF_SERVICE("view orders of the service"),
    DELETE_ORDERS_OF_SERVICE("delete orders of the service");

    private final String type;

    UserOperation(String type) {
        this.type = type;
    }

    /** For UserOperation deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
