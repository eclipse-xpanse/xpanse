/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.eclipse.xpanse.api.response.ErrCode;
import org.eclipse.xpanse.api.response.ErrResponse;
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
    public ErrResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append("：").append(fieldError.getDefaultMessage())
                    .append(", ");
        }
        return new ErrResponse(ErrCode.BAD_PARAMETERS, sb.toString());
    }

    /**
     * Exception handler for ConstraintViolationException.
     */
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrResponse handleConstraintViolationException(ConstraintViolationException ex) {
        String failMessage = ex.getMessage();
        return new ErrResponse(ErrCode.BAD_PARAMETERS, failMessage);
    }

    /**
     * Exception handler for RuntimeException.
     */
    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrResponse handleRuntimeException(RuntimeException ex) {
        String failMessage = ex.getMessage();
        return new ErrResponse(ErrCode.RUNTIME_ERROR, failMessage);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrResponse handleNotFoundException(EntityNotFoundException ex) {
        String failMessage = ex.getMessage();
        return new ErrResponse(ErrCode.BAD_PARAMETERS, failMessage);
    }
}