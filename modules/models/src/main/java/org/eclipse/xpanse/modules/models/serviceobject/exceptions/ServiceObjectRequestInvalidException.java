/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceobject.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when service configuration is invalid. */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceObjectRequestInvalidException extends RuntimeException {

    private final List<String> errorReasons;

    public ServiceObjectRequestInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
