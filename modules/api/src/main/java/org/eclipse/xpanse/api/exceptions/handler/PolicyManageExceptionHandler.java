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
    public Response handlePolicyNotFoundException(
            PolicyNotFoundException ex) {
        return getErrorResponse(ResultType.POLICY_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for PolicyDuplicateException.
     */
    @ExceptionHandler({PolicyDuplicateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handlePolicyDuplicateException(
            PolicyDuplicateException ex) {
        return getErrorResponse(ResultType.POLICY_DUPLICATE,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for PoliciesValidationFailedException.
     */
    @ExceptionHandler({PoliciesValidationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handlePoliciesValidationFailedException(
            PoliciesValidationFailedException ex) {
        return getErrorResponse(ResultType.POLICY_VALIDATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }


    /**
     * Exception handler for PoliciesEvaluationFailedException.
     */
    @ExceptionHandler({PoliciesEvaluationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handlePoliciesEvaluationFailedException(
            PoliciesEvaluationFailedException ex) {
        return getErrorResponse(ResultType.POLICY_EVALUATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }
}
