/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.eclipse.xpanse.modules.models.common.exceptions.ResponseInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Response bean validation based on AspectJ.
 */
@Aspect
@Component
@Slf4j
public class ResponseValidator {

    private final Validator validator;

    public ResponseValidator(@Autowired Validator validator) {
        this.validator = validator;
    }

    @AfterReturning(
            pointcut = "execution(* org.eclipse.xpanse.api.controllers..*(..))",
            returning = "result")
    public void validateResponseData(Object result) {
        validateResponse(result);
    }

    private void validateResponse(Object object) {
        if (!Objects.isNull(object)) {
            List<String> errors = new ArrayList<>();
            if (object instanceof Collection<?>) {
                ((Collection<?>) object).forEach(item -> {
                    Set<ConstraintViolation<Object>> validationResults = validator.validate(item);
                    if (!validationResults.isEmpty()) {
                        for (ConstraintViolation<Object> error : validationResults) {
                            errors.add(error.getPropertyPath() + ":" + error.getMessage());
                        }
                    }

                });
            } else {
                Set<ConstraintViolation<Object>> validationResults = validator.validate(object);
                if (!validationResults.isEmpty()) {
                    for (ConstraintViolation<Object> error : validationResults) {
                        errors.add(error.getPropertyPath() + ":" + error.getMessage());
                    }
                }
            }
            if (!errors.isEmpty()) {
                throw new ResponseInvalidException(errors);
            }
        }
    }
}
