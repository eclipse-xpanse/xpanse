/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.state;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.RuntimeState;

/**
 * Describes the list of resources that are deployed as part of managed service deployment.
 */
@Data
public class OclResource {

    RuntimeState state = RuntimeState.INACTIVE;
    String id = "";
    String type = "";
    String name = "";

    Map<String, String> properties = new HashMap<>();
}
