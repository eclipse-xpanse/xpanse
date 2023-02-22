/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.eclipse.xpanse.api.response.ErrCode;
import org.eclipse.xpanse.api.response.Response;
import org.springframework.http.HttpStatus;
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
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * Exception handler for MethodArgumentNotValidException.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append("：").append(fieldError.getDefaultMessage())
                .append(", ");
        }
        return Response.errorResponse(ErrCode.BAD_PARAMETERS, sb.toString());
    }

    /**
     * Exception handler for ConstraintViolationException.
     */
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleConstraintViolationException(ConstraintViolationException ex) {
        String failMessage = ex.getMessage();
        return Response.errorResponse(ErrCode.BAD_PARAMETERS, failMessage);
    }

    /**
     * Exception handler for RuntimeException.
     */
    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleRuntimeException(RuntimeException ex) {
        String failMessage = ex.getMessage();
        return Response.errorResponse(ErrCode.RUNTIME_ERROR, failMessage);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handleNotFoundException(EntityNotFoundException ex) {
        String failMessage = ex.getMessage();
        return Response.errorResponse(ErrCode.BAD_PARAMETERS, failMessage);
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handleException(Exception ex) {
        String failMessage = ex.getClass().getName() + ":" + ex.getMessage();
        return Response.errorResponse(ErrCode.RUNTIME_ERROR, failMessage);
    }
}