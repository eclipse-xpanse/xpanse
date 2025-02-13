/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange;

import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.AnsibleScriptConfig;

/** Request object to update service change. */
@Data
public class ServiceChangeRequest {

    private UUID changeId;
    private Map<String, Object> serviceChangeParameters;
    private AnsibleScriptConfig ansibleScriptConfig;
    private Map<String, Object> ansibleInventory;
}
