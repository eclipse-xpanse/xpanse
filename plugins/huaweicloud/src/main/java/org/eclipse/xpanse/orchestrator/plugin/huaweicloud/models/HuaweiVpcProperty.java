/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.models;

import java.util.HashMap;

/**
 * Huawei cloud vpc property.
 */
public class HuaweiVpcProperty extends HashMap<String, String> {

    /**
     * Init method to put property key and value.
     */
    public HuaweiVpcProperty() {
        this.put("vpc", "vpc_id");
        this.put("subnet", "subnet_id");
    }
}