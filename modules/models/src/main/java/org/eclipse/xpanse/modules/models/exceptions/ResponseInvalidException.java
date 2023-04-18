/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *  Exception thrown when response beans does not match our own data model.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseInvalidException extends RuntimeException {
    private List<String> errorReasons;

    public ResponseInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
