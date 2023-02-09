/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.SecurityRuleAction;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.SecurityRuleDirection;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.SecurityRuleProtocol;

/**
 * Defines security rules to be applied for the managed service.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityRule extends RuntimeBase {

    private String name;
    private Integer priority;
    private SecurityRuleProtocol protocol;
    private String cidr;
    private SecurityRuleDirection direction;
    private String ports;
    private SecurityRuleAction action;

}
