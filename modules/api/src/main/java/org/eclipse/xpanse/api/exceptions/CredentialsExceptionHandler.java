/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.exceptions.ResponseInvalidException;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialCapabilityNotFound;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialVariablesNotComplete;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to Credentials center for the REST API.
 */
@Slf4j
@RestControllerAdvice
public class CredentialsExceptionHandler {

    /**
     * Exception handler for CredentialCapabilityNotFound.
     */
    @ExceptionHandler({CredentialCapabilityNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleCredentialCapabilityNotFound(
            ResponseInvalidException ex) {
        return Response.errorResponse(ResultType.CREDENTIAL_CAPABILITY_NOT_FOUND,
                ex.getErrorReasons());
    }

    /**
     * Exception handler for CredentialsNotFoundException.
     */
    @ExceptionHandler({CredentialsNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleCredentialsNotFoundException(
            ResponseInvalidException ex) {
        return Response.errorResponse(ResultType.CREDENTIALS_NOT_FOUND,
                ex.getErrorReasons());
    }

    /**
     * Exception handler for CredentialVariablesNotComplete.
     */
    @ExceptionHandler({CredentialVariablesNotComplete.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleCredentialVariablesNotComplete(
            ResponseInvalidException ex) {
        return Response.errorResponse(ResultType.CREDENTIALS_VARIABLES_NOT_COMPLETE,
                ex.getErrorReasons());
    }
}
