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
 * Defines the console information for the managed service.
 */
@Data
public class Console {

    private String backend;
    private Map<String, Object> properties = new HashMap<>();

}
