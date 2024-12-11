/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when a deployment variable schema definition is invalid. */
@EqualsAndHashCode(callSuper = true)
@Data
public class InvalidValueSchemaException extends RuntimeException {

    private final List<String> invalidValueSchemaKeys;

    public InvalidValueSchemaException(List<String> invalidValueSchemaKeys) {
        this.invalidValueSchemaKeys = invalidValueSchemaKeys;
    }
}
