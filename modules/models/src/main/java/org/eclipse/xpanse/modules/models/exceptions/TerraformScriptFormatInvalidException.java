/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when Terraform script is invalid.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TerraformScriptFormatInvalidException extends RuntimeException {
    private List<String> errorReasons;

    public TerraformScriptFormatInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
