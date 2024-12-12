/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when response beans do not match our own data model. */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseInvalidException extends RuntimeException {
    private final List<String> errorReasons;

    public ResponseInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
