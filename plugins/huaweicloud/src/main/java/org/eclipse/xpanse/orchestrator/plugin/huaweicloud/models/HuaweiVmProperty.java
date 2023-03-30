/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.models;

import java.util.HashMap;

/**
 * Huawei cloud vm property.
 */
public class HuaweiVmProperty extends HashMap<String, String> {

    /**
     * Init method to put property key and value.
     */
    public HuaweiVmProperty() {
        this.put("ip", "access_ip_v4");
        this.put("image_id", "image_id");
        this.put("image_name", "image_name");
        this.put("region", "region");
    }
}