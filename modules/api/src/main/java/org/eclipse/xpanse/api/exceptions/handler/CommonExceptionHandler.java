/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.ORDER_ID;
import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.SERVICE_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientAuthenticationFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.GitRepoCloneException;
import org.eclipse.xpanse.modules.models.common.exceptions.ResponseInvalidException;
import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.common.exceptions.UserNotLoggedInException;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.response.OrderFailedErrorResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Exception handler for the REST API. */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get error response.
     *
     * @param errorType resultType
     * @param details details
     * @return Response
     */
    public static ErrorResponse getErrorResponse(ErrorType errorType, List<String> details) {
        if (StringUtils.isNotBlank(MDC.get(SERVICE_ID))) {
            OrderFailedErrorResponse orderFailedResponse =
                    OrderFailedErrorResponse.errorResponse(errorType, details);
            orderFailedResponse.setServiceId(MDC.get(SERVICE_ID));
            orderFailedResponse.setOrderId(MDC.get(ORDER_ID));
            return orderFailedResponse;
        } else {
            return ErrorResponse.errorResponse(errorType, details);
        }
    }

    /** Exception handler for MethodArgumentNotValidException. */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("handleMethodArgumentNotValidException: ", ex);
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String errorMsg = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            Object values = fieldError.getRejectedValue();
            if (Objects.nonNull(fieldError.getCodes())) {
                List<String> errorCodeList = Arrays.asList(fieldError.getCodes());
                String annotationName = errorCodeList.getLast();
                if (StringUtils.equals(annotationName, "UniqueElements")) {
                    if (Objects.nonNull(values) && values instanceof List) {
                        String errorValues = findDuplicatesItemsString((List<Object>) values);
                        errorMsg =
                                fieldError.getField()
                                        + " with duplicate items: "
                                        + errorValues
                                        + ". Violating constraint "
                                        + annotationName;
                    }
                } else {
                    errorMsg =
                            fieldError.getField()
                                    + " with invalid value: "
                                    + values
                                    + ". Violating constraint "
                                    + annotationName;
                }
            }
            errors.add(errorMsg);
            errors.sort(String::compareTo);
        }
        return getErrorResponse(ErrorType.UNPROCESSABLE_ENTITY, errors);
    }

    /** Exception handler for RuntimeException. */
    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException ex) {
        String failMessage = ex.getMessage();
        log.error("handleRuntimeException: ", ex);
        return getErrorResponse(ErrorType.RUNTIME_ERROR, Collections.singletonList(failMessage));
    }

    /** Exception handler for HttpMessageConversionException. */
    @ExceptionHandler({HttpMessageConversionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageConversionException(HttpMessageConversionException ex) {
        log.error("handleHttpMessageConversionException: ", ex);
        String failMessage = ex.getMessage();
        return getErrorResponse(ErrorType.BAD_PARAMETERS, Collections.singletonList(failMessage));
    }

    /** Exception handler for IllegalArgumentException. */
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("handleIllegalArgumentException: ", ex);
        String failMessage = ex.getMessage();
        return getErrorResponse(ErrorType.BAD_PARAMETERS, Collections.singletonList(failMessage));
    }

    /** Exception handler for Exception. */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        log.error("handleException: ", ex);
        String failMessage = ex.getClass().getName() + ":" + ex.getMessage();
        return getErrorResponse(ErrorType.RUNTIME_ERROR, Collections.singletonList(failMessage));
    }

    /** Exception handler for ResponseInvalidException. */
    @ExceptionHandler({ResponseInvalidException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResponse handleResponseInvalidException(ResponseInvalidException ex) {
        return getErrorResponse(ErrorType.INVALID_RESPONSE, ex.getErrorReasons());
    }

    /** Exception handler for ResponseInvalidException. */
    @ExceptionHandler({XpanseUnhandledException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleXpanseUnhandledException(XpanseUnhandledException ex) {
        return getErrorResponse(
                ErrorType.UNHANDLED_EXCEPTION, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for AccessDeniedException. */
    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        return getErrorResponse(
                ErrorType.ACCESS_DENIED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for SensitiveFieldEncryptionOrDecryptionFailedException. */
    @ExceptionHandler({SensitiveFieldEncryptionOrDecryptionFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleSensitiveFieldEncryptionOrDecryptionFailedException(
            SensitiveFieldEncryptionOrDecryptionFailedException ex) {
        return getErrorResponse(
                ErrorType.SENSITIVE_FIELD_ENCRYPTION_DECRYPTION_EXCEPTION,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for UnsupportedEnumValueException. */
    @ExceptionHandler({UnsupportedEnumValueException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResponse handleUnsupportedEnumValueException(UnsupportedEnumValueException ex) {
        return getErrorResponse(
                ErrorType.UNSUPPORTED_ENUM_VALUE, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for MethodArgumentTypeMismatchException. */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResponse handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        return getErrorResponse(
                ErrorType.UNPROCESSABLE_ENTITY, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for UserNotLoggedInException. */
    @ExceptionHandler({UserNotLoggedInException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorResponse handleUserNoLoginException(UserNotLoggedInException ex) {
        return getErrorResponse(
                ErrorType.USER_NO_LOGIN_EXCEPTION, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for GitRepoCloneException. */
    @ExceptionHandler({GitRepoCloneException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleGitRepoCloneException(GitRepoCloneException ex) {
        log.error("GitRepoCloneException: ", ex);
        String failMessage = ex.getMessage();
        return getErrorResponse(
                ErrorType.INVALID_GIT_REPO_DETAILS, Collections.singletonList(failMessage));
    }

    /** Exception handler for ClientApiCallFailedException. */
    @ExceptionHandler({ClientApiCallFailedException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ResponseBody
    public ErrorResponse handleClientApiCalledException(ClientApiCallFailedException ex) {
        return getErrorResponse(
                ErrorType.BACKEND_FAILURE, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ClientAuthenticationFailedException. */
    @ExceptionHandler({ClientAuthenticationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ResponseBody
    public ErrorResponse handleClientAuthException(ClientAuthenticationFailedException ex) {
        return getErrorResponse(
                ErrorType.BACKEND_FAILURE, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for NoResourceFoundException. */
    @ExceptionHandler({NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNoResourceFoundException(NoResourceFoundException ex) {
        return getErrorResponse(
                ErrorType.RESOURCE_NOT_FOUND, Collections.singletonList(ex.getResourcePath()));
    }

    private String findDuplicatesItemsString(List<Object> list) {
        if (!CollectionUtils.isEmpty(list)) {
            List<Object> duplicates = new ArrayList<>();
            Set<Object> set = new HashSet<>();
            for (Object item : list) {
                if (set.contains(item)) {
                    duplicates.add(item);
                } else {
                    set.add(item);
                }
            }
            if (!CollectionUtils.isEmpty(duplicates)) {
                List<String> duplicatesItems = new ArrayList<>();
                duplicates.forEach(item -> duplicatesItems.add(getItemString(item)));
                return duplicatesItems.toString();
            }
        }
        return StringUtils.EMPTY;
    }

    private String getItemString(Object item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert item to json string:{}", item, e);
            return item.toString();
        }
    }
}
