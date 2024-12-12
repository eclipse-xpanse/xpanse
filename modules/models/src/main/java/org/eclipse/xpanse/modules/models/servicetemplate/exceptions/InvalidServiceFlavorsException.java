/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import java.io.Serial;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when a deployment service version is invalid. */
@EqualsAndHashCode(callSuper = true)
@Data
public class InvalidServiceFlavorsException extends RuntimeException {
    @Serial private static final long serialVersionUID = 8442465023886046312L;

    private final List<String> errorReasons;

    public InvalidServiceFlavorsException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
