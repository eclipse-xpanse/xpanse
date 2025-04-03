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
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectChangeOrderAlreadyExistsException;
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectNotFoundException;
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectRequestInvalidException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler related to service object. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ServiceObjectRequestExceptionHandler {

    /** Exception handler for ServiceObjectNotFoundException. */
    @ExceptionHandler({ServiceObjectNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceObjectNotFoundException(ServiceObjectNotFoundException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_OBJECT_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceObjectRequestInvalidException. */
    @ExceptionHandler({ServiceObjectRequestInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceObjectRequestInvalidException(
            ServiceObjectRequestInvalidException ex) {
        log.error("Service object request is invalid: {}", ex.getErrorReasons());
        return getErrorResponse(ErrorType.INVALID_SERVICE_OBJECT_REQUEST, ex.getErrorReasons());
    }

    /** Exception handler for ServiceObjectManageOrderAlreadyExistsException. */
    @ExceptionHandler({ServiceObjectChangeOrderAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceObjectsChangeOrderAlreadyExistsException(
            ServiceObjectChangeOrderAlreadyExistsException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_OBJECT_CHANGE_ORDER_ALREADY_EXISTS,
                Collections.singletonList(ex.getMessage()));
    }
}
