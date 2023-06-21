/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * Vpc model.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Vpc extends DeployResource {

    public String vpc;
    public String subnet;
}