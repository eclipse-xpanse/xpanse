/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when service configuration is invalid.
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceConfigurationInvalidException extends RuntimeException {

    private final List<String> errorReasons;

    public ServiceConfigurationInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
