/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange;

import java.util.List;
import lombok.Data;

/** The result of Service change. */
@Data
public class ServiceChangeResult {

    private Boolean isSuccessful;
    private String error;
    private List<AnsibleTaskResult> tasks;
}
