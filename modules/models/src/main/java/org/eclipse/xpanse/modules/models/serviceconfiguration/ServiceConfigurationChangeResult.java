/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import java.util.List;
import lombok.Data;

/** The result of Service Configuration change. */
@Data
public class ServiceConfigurationChangeResult {

    private Boolean isSuccessful;
    private String error;
    private List<AnsibleTaskResult> tasks;
}
