/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler.getErrorResponse;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.servicechange.exceptions.ServiceChangeRequestEntityNotFoundException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceActionChangeOrderAlreadyExistsException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigChangeOrderAlreadyExistsException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler related to service configuration. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ServiceConfigurationExceptionHandler {

    /** Exception handler for ServiceConfigurationNotValidException. */
    @ExceptionHandler({ServiceConfigurationInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceConfigurationInvalidException(
            ServiceConfigurationInvalidException ex) {
        return getErrorResponse(ErrorType.INVALID_SERVICE_CONFIGURATION, ex.getErrorReasons());
    }

    /** Exception handler for ServiceChangeRequestEntityNotFoundException. */
    @ExceptionHandler({ServiceChangeRequestEntityNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceConfigurationUpdateRequestNotFoundException(
            ServiceChangeRequestEntityNotFoundException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_CONFIG_UPDATE_REQUEST_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceConfigurationNotFoundException. */
    @ExceptionHandler({ServiceConfigurationNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceConfigurationNotFoundException(
            ServiceConfigurationNotFoundException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_CONFIGURATION_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceConfigChangeOrderAlreadyExistsException. */
    @ExceptionHandler({ServiceConfigChangeOrderAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceConfigChangeOrderAlreadyExistsException(
            ServiceConfigChangeOrderAlreadyExistsException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_CONFIG_CHANGE_ORDER_ALREADY_EXISTS,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceActionChangeOrderAlreadyExistsException. */
    @ExceptionHandler({ServiceActionChangeOrderAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceActionChangeOrderAlreadyExistsException(
            ServiceActionChangeOrderAlreadyExistsException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_ACTION_CHANGE_ORDER_ALREADY_EXISTS,
                Collections.singletonList(ex.getMessage()));
    }
}
