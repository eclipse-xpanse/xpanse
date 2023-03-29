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
        this.put("region", "region");

        this.put("flavor_name", "flavor_name");
        this.put("image_name", "image_name");
        this.put("image_id", "image_id");
        this.put("system_disk_size", "system_disk_size");
        this.put("system_disk_type", "system_disk_type");

    }


}
