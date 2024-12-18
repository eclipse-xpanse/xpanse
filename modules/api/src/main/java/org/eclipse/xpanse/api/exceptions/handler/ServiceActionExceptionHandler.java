/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler.getErrorResponse;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceActionTemplateInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler related to service action. */
@Slf4j
@RestControllerAdvice
public class ServiceActionExceptionHandler {

    /** Exception handler for ServiceActionInvalidException. */
    @ExceptionHandler({ServiceActionTemplateInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceActionInvalidException(
            ServiceActionTemplateInvalidException ex) {
        return getErrorResponse(ErrorType.INVALID_SERVICE_ACTION, ex.getErrorReasons());
    }
}
