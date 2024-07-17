/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.api.exceptions.handler;


import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to service configuration.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ServiceConfigurationExceptionHandler {

    /**
     * Exception handler for ServiceConfigurationNotValidException.
     */
    @ExceptionHandler({ServiceConfigurationInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceConfigurationInvalidException(
            ServiceConfigurationInvalidException ex) {
        return Response.errorResponse(ResultType.INVALID_SERVICE_CONFIGURATION,
                Collections.singletonList(ex.getMessage()));
    }
}
