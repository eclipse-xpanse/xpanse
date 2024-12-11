/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/** Exception throw when variables validation fails. */
@EqualsAndHashCode(callSuper = true)
@Data
public class VariableValidationFailedException extends RuntimeException {
    private final List<String> errorReasons;

    public VariableValidationFailedException(List<String> errorReasons) {
        super(String.format("Variable validation failed: %s", StringUtils.join(errorReasons)));
        this.errorReasons = errorReasons;
    }
}
