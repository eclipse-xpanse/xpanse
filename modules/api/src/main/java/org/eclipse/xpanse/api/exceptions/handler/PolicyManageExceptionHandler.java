/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler.getErrorResponse;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to Credentials center for the REST API.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class PolicyManageExceptionHandler {

    /**
     * Exception handler for PolicyNotFoundException.
     */
    @ExceptionHandler({PolicyNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handlePolicyNotFoundException(
            PolicyNotFoundException ex) {
        return getErrorResponse(ErrorType.POLICY_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for PolicyDuplicateException.
     */
    @ExceptionHandler({PolicyDuplicateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handlePolicyDuplicateException(
            PolicyDuplicateException ex) {
        return getErrorResponse(ErrorType.POLICY_DUPLICATE,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for PoliciesValidationFailedException.
     */
    @ExceptionHandler({PoliciesValidationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handlePoliciesValidationFailedException(
            PoliciesValidationFailedException ex) {
        return getErrorResponse(ErrorType.POLICY_VALIDATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }


    /**
     * Exception handler for PoliciesEvaluationFailedException.
     */
    @ExceptionHandler({PoliciesEvaluationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handlePoliciesEvaluationFailedException(
            PoliciesEvaluationFailedException ex) {
        return getErrorResponse(ErrorType.POLICY_EVALUATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }
}
