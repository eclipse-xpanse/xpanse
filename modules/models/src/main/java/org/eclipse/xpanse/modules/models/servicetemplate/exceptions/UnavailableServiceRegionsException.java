/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when a deployment service region is unavailable.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UnavailableServiceRegionsException extends RuntimeException {

    private final List<String> errorReasons;

    public UnavailableServiceRegionsException(List<String> message) {
        this.errorReasons = message;
    }
}
