/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import lombok.Data;

/**
 * The result of ansible task.
 */
@Data
public class AnsibleTaskResult {
    private String name;
    private Boolean isSuccessful;
    private String message;
}
