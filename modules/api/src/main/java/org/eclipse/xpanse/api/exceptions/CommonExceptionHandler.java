/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.response.Response;
import org.eclipse.xpanse.api.response.ResultType;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Exception handler for the REST API.
 */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * Exception handler for MethodArgumentNotValidException.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("handleMethodArgumentNotValidException: ", ex);
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append("：").append(fieldError.getDefaultMessage())
                .append(", ");
        }
        return Response.errorResponse(ResultType.BAD_PARAMETERS, sb.toString());
    }

    /**
     * Exception handler for ConstraintViolationException.
     */
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("handleConstraintViolationException: ", ex);
        String failMessage = ex.getMessage();
        return Response.errorResponse(ResultType.BAD_PARAMETERS, failMessage);
    }

    /**
     * Exception handler for RuntimeException.
     */
    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleRuntimeException(RuntimeException ex) {
        String failMessage = ex.getMessage();
        log.error("handleRuntimeException: ", ex);
        return Response.errorResponse(ResultType.RUNTIME_ERROR, failMessage);
    }

    /**
     * Exception handler for HttpMessageConversionException.
     */
    @ExceptionHandler({HttpMessageConversionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleHttpMessageConversionException(HttpMessageConversionException ex) {
        log.error("handleHttpMessageConversionException: ", ex);
        String failMessage = ex.getMessage();
        return Response.errorResponse(ResultType.BAD_PARAMETERS, failMessage);
    }

    /**
     * Exception handler for EntityNotFoundException.
     */
    @ExceptionHandler({EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handleNotFoundException(EntityNotFoundException ex) {
        log.error("handleNotFoundException: ", ex);
        String failMessage = ex.getMessage();
        return Response.errorResponse(ResultType.BAD_PARAMETERS, failMessage);
    }

    /**
     * Exception handler for IllegalArgumentException.
     */
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("handleNotFoundException: ", ex);
        String failMessage = ex.getMessage();
        return Response.errorResponse(ResultType.BAD_PARAMETERS, failMessage);
    }

    /**
     * Exception handler for Exception.
     */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handleException(Exception ex) {
        log.error("handleException: ", ex);
        String failMessage = ex.getClass().getName() + ":" + ex.getMessage();
        return Response.errorResponse(ResultType.RUNTIME_ERROR, failMessage);
    }
}