/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.register.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceAlreadyRegisteredException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceNotRegisteredException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.service.register.exceptions.TerraformScriptFormatInvalidException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to service registration requests.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RegistrationExceptionHandler {

    /**
     * Exception handler for ServiceAlreadyRegisteredException.
     */
    @ExceptionHandler({ServiceAlreadyRegisteredException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceAlreadyRegisteredException(
            ServiceAlreadyRegisteredException ex) {
        return Response.errorResponse(ResultType.SERVICE_ALREADY_REGISTERED,
                Collections.singletonList(ex.getMessage()));

    }

    /**
     * Exception handler for IconProcessingFailedException.
     */
    @ExceptionHandler({IconProcessingFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleIconProcessingFailedException(
            IconProcessingFailedException ex) {
        return Response.errorResponse(ResultType.ICON_PROCESSING_FAILED,
                Collections.singletonList(ex.getMessage()));

    }

    /**
     * Exception handler for ServiceRequestedNotFoundException.
     */
    @ExceptionHandler({ServiceNotRegisteredException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceNotRegisteredException(
            ServiceNotRegisteredException ex) {
        return Response.errorResponse(ResultType.SERVICE_NOT_REGISTERED,
                Collections.singletonList(ex.getMessage()));

    }

    /**
     * Exception handler for TerraformScriptFormatInvalidException.
     */
    @ExceptionHandler({TerraformScriptFormatInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleTerraformScriptFormatInvalidException(
            TerraformScriptFormatInvalidException ex) {
        return Response.errorResponse(ResultType.TERRAFORM_SCRIPT_INVALID, ex.getErrorReasons());
    }

    /**
     * Exception handler for handleServiceUpdateNotAllowed.
     */
    @ExceptionHandler({ServiceUpdateNotAllowed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceUpdateNotAllowed(
            ServiceUpdateNotAllowed ex) {
        return Response.errorResponse(ResultType.SERVICE_UPDATE_NOT_ALLOWED,
                Collections.singletonList(ex.getMessage()));
    }
}
