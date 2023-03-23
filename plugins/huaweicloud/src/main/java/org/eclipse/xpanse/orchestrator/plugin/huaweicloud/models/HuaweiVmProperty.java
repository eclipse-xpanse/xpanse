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
        // TODO put all Vm filed Name and tfStateFile key like this.
        this.put("ip", "access_ip_v4");

        // TODO put tfStateFile key except all filed Name in Vm.
        this.put("aaaa", null);
    }


}
