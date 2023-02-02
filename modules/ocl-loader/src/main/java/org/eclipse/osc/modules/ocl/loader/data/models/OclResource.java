/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * Describes the list of resources that are deployed as part of managed service deployment.
 */
@Data
public class OclResource {

    String state = "inactive";
    String id = "";
    String type = "";
    String name = "";

    Map<String, String> properties = new HashMap<>();
}
