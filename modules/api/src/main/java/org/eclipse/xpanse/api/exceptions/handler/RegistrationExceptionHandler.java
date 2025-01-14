/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler.getErrorResponse;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.exceptions.InvalidBillingConfigException;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceVersionException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.MandatoryValueMissingForFixedVariablesException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUnavailableException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.UnavailableServiceRegionsException;
import org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions.ReviewServiceTemplateRequestNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions.ServiceTemplateRequestNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions.ServiceTemplateRequestNotFound;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler related to service registration requests. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RegistrationExceptionHandler {

    /** Exception handler for IconProcessingFailedException. */
    @ExceptionHandler({IconProcessingFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleIconProcessingFailedException(IconProcessingFailedException ex) {
        return getErrorResponse(
                ErrorType.ICON_PROCESSING_FAILED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceRequestedNotFoundException. */
    @ExceptionHandler({ServiceTemplateNotRegistered.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateNotRegisteredException(
            ServiceTemplateNotRegistered ex) {
        return getErrorResponse(
                ErrorType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for TerraformScriptFormatInvalidException. */
    @ExceptionHandler({TerraformScriptFormatInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleTerraformScriptFormatInvalidException(
            TerraformScriptFormatInvalidException ex) {
        return getErrorResponse(ErrorType.TERRAFORM_SCRIPT_INVALID, ex.getErrorReasons());
    }

    /** Exception handler for ServiceTemplateRequestNotAllowed. */
    @ExceptionHandler({ServiceTemplateRequestNotAllowed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateRequestNotAllowed(
            ServiceTemplateRequestNotAllowed ex) {
        return getErrorResponse(
                ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceTemplateRequestNotAllowed. */
    @ExceptionHandler({InvalidValueSchemaException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidValueSchemaException(InvalidValueSchemaException ex) {
        return getErrorResponse(
                ErrorType.VARIABLE_SCHEMA_DEFINITION_INVALID, ex.getInvalidValueSchemaKeys());
    }

    /** Exception handler for ServiceTemplateUnavailableException. */
    @ExceptionHandler({ServiceTemplateUnavailableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateDisabledException(
            ServiceTemplateUnavailableException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_TEMPLATE_UNAVAILABLE, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for InvalidServiceVersionException. */
    @ExceptionHandler({InvalidServiceVersionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidServiceVersionException(InvalidServiceVersionException ex) {
        return getErrorResponse(
                ErrorType.INVALID_SERVICE_VERSION, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for InvalidServiceFlavorsException. */
    @ExceptionHandler({InvalidServiceFlavorsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidServiceFlavorsException(InvalidServiceFlavorsException ex) {
        return getErrorResponse(ErrorType.INVALID_SERVICE_FLAVORS, ex.getErrorReasons());
    }

    /** Exception handler for MandatoryValueMissingForFixedVariablesException. */
    @ExceptionHandler({MandatoryValueMissingForFixedVariablesException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleMandatoryValueMissingForFixedVariablesException(
            MandatoryValueMissingForFixedVariablesException ex) {
        return getErrorResponse(
                ErrorType.MANDATORY_VALUE_MISSING, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for UnavailableServiceRegionsException. */
    @ExceptionHandler({UnavailableServiceRegionsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleUnavailableServiceRegionsException(
            UnavailableServiceRegionsException ex) {
        return getErrorResponse(ErrorType.UNAVAILABLE_SERVICE_REGIONS, ex.getErrorReasons());
    }

    /** Exception handler for InvalidBillingConfigException. */
    @ExceptionHandler({InvalidBillingConfigException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidBillingConfigException(InvalidBillingConfigException ex) {
        return getErrorResponse(ErrorType.INVALID_BILLING_CONFIG, ex.getErrorReasons());
    }

    /** Exception handler for ServiceTemplateRequestNotFound. */
    @ExceptionHandler({ServiceTemplateRequestNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateRequestNotFoundException(
            ServiceTemplateRequestNotFound ex) {
        return getErrorResponse(
                ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ReviewServiceTemplateRequestNotAllowed. */
    @ExceptionHandler({ReviewServiceTemplateRequestNotAllowed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleReviewServiceTemplateRequestNotAllowed(
            ReviewServiceTemplateRequestNotAllowed ex) {
        return getErrorResponse(
                ErrorType.REVIEW_SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED,
                Collections.singletonList(ex.getMessage()));
    }
}
