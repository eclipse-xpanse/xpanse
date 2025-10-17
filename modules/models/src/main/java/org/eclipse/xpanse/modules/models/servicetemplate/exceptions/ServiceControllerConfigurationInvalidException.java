/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when the OCL contains service controller configuration which causes errors in
 * controller OpenAPI file generation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceControllerConfigurationInvalidException extends RuntimeException {

    private final List<String> errorReasons;

    public ServiceControllerConfigurationInvalidException(List<String> errorReasons) {
        this.errorReasons = errorReasons;
    }
}
