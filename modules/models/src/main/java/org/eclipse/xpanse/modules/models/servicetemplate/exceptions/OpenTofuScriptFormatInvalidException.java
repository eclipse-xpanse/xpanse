/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when an OpenTofu script is invalid.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuScriptFormatInvalidException extends RuntimeException {
    private final List<String> errorReasons;

    public OpenTofuScriptFormatInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
