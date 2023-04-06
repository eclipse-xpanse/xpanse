/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resource;

import lombok.Data;

/**
 * TfOutput class.
 */
@Data
public class TfOutput {

    private String type;
    private String value;
}
