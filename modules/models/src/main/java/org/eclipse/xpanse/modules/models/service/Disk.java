/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * Vm model.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Disk extends DeployResource {

    public String size;
    public String type;

}
