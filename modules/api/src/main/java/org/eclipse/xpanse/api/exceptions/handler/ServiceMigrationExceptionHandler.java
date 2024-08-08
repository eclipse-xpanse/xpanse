/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.api.exceptions.handler;


import static org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler.getErrorResponse;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceMigrationNotFoundException;
import org.eclipse.xpanse.modules.models.workflow.migrate.exceptions.ServiceMigrationFailedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to service migration.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ServiceMigrationExceptionHandler {

    /**
     * Exception handler for ServiceMigrationFailedException.
     */
    @ExceptionHandler({ServiceMigrationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceNotMigrationException(ServiceMigrationFailedException ex) {
        return getErrorResponse(ResultType.SERVICE_MIGRATION_FAILED_EXCEPTION,
                Collections.singletonList(ex.getMessage()));
    }


    @ExceptionHandler({ServiceMigrationNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceNotMigrationException(ServiceMigrationNotFoundException ex) {
        return getErrorResponse(ResultType.SERVICE_MIGRATION_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }
}
