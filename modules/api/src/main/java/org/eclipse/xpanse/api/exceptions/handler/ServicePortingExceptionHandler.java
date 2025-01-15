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
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServicePortingNotFoundException;
import org.eclipse.xpanse.modules.models.workflow.serviceporting.exceptions.ServicePortingFailedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler related to service porting. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ServicePortingExceptionHandler {

    /** Exception handler for ServicePortingFailedException. */
    @ExceptionHandler({ServicePortingFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServicePortingFailedException(ServicePortingFailedException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_PORTING_FAILED_EXCEPTION,
                Collections.singletonList(ex.getMessage()));
    }

    @ExceptionHandler({ServicePortingNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceNotPortingException(ServicePortingNotFoundException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_PORTING_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }
}
