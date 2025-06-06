/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceaction.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when service action is invalid. */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceActionTemplateInvalidException extends RuntimeException {

    private final List<String> errorReasons;

    public ServiceActionTemplateInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
