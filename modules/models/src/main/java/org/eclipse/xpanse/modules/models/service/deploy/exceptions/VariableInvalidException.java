/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception throw when variables validation fails.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VariableInvalidException extends RuntimeException {
    private final List<String> errorReasons;

    public VariableInvalidException(List<String> errorReasons) {
        super("Variable validation failed: " + errorReasons);
        this.errorReasons = errorReasons;
    }

}