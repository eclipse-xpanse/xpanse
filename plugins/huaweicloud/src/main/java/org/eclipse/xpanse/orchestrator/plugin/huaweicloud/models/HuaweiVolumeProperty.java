/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.models;

import java.util.HashMap;

/**
 * Huawei cloud volume property.
 */
public class HuaweiVolumeProperty extends HashMap<String, String> {

    /**
     * Init method to put property key and value.
     */
    public HuaweiVolumeProperty() {
        this.put("size", "size");
        this.put("type", "volume_type");
    }
}