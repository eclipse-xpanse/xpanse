/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;


import static org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler.getErrorResponse;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.monitor.exceptions.MetricsDataNotYetAvailableException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotSupportedForMonitoringException;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to monitoring requests.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ServiceMetricsExceptionHandler {

    /**
     * Exception handler for ResourceNotSupportedForMonitoringException.
     */
    @ExceptionHandler({ResourceNotSupportedForMonitoringException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleResourceNotSupportedForMonitoringException(
            ResourceNotSupportedForMonitoringException ex) {
        return getErrorResponse(ResultType.RESOURCE_TYPE_INVALID_FOR_MONITORING,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ResourceNotFoundException.
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleResourceNotFoundException(ResourceNotFoundException ex) {
        return getErrorResponse(ResultType.RESOURCE_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for MetricsDataNotYetAvailableException.
     */
    @ExceptionHandler({MetricsDataNotYetAvailableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleMetricsDataNotYetAvailableException(
            MetricsDataNotYetAvailableException ex) {
        return getErrorResponse(ResultType.METRICS_DATA_NOT_READY,
                Collections.singletonList(ex.getMessage()));
    }
}
