/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OclResource {

    String state = "inactive";
    String id = "";
    String type = "";
    String name = "";

    Map<String, String> properties = new HashMap<>();
}
