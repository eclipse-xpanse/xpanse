/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.AnsibleScriptConfig;

/**
 * Request object to update service configuration.
 */
@Data
public class ServiceConfigurationChangeRequest {

    private UUID changeId;
    private Map<String, Object> configParameters;
    private AnsibleScriptConfig ansibleScriptConfig;
    private Map<String, Object> ansibleInventory;
}
