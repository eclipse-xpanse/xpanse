/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.policy.userpolicy;

import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Csp;

/** The query model for search policies. */
@Data
public class UserPolicyQueryRequest {

    private Boolean enabled;

    private String userId;

    private Csp csp;

    private String policy;
}
