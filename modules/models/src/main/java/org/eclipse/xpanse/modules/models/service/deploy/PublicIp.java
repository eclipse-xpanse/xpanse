/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * PublicIp model.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PublicIp extends DeployResource {

    public String ip;
}