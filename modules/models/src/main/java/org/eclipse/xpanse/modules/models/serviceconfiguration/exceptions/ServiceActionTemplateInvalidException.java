/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions;

import java.util.List;
import lombok.Data;

/** Exception thrown when service action is invalid. */
@Data
public class ServiceActionTemplateInvalidException extends RuntimeException {

    private final List<String> errorReasons;

    public ServiceActionTemplateInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
