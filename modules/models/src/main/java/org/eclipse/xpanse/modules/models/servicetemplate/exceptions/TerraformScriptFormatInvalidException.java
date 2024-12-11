/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when a Terraform script is invalid. */
@EqualsAndHashCode(callSuper = true)
@Data
public class TerraformScriptFormatInvalidException extends RuntimeException {
    private final List<String> errorReasons;

    public TerraformScriptFormatInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
