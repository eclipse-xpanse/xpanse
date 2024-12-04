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
import org.eclipse.xpanse.modules.models.servicetemplate.change.exceptions.ServiceTemplateChangeRequestNotFound;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceVersionException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyReviewed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateDisabledException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateStillInUseException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.UnavailableServiceRegionsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to service registration requests.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RegistrationExceptionHandler {

    /**
     * Exception handler for ServiceAlreadyRegisteredException.
     */
    @ExceptionHandler({ServiceTemplateAlreadyRegistered.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateAlreadyRegisteredException(
            ServiceTemplateAlreadyRegistered ex) {
        return getErrorResponse(ErrorType.SERVICE_TEMPLATE_ALREADY_REGISTERED,
                Collections.singletonList(ex.getMessage()));

    }

    /**
     * Exception handler for IconProcessingFailedException.
     */
    @ExceptionHandler({IconProcessingFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleIconProcessingFailedException(
            IconProcessingFailedException ex) {
        return getErrorResponse(ErrorType.ICON_PROCESSING_FAILED,
                Collections.singletonList(ex.getMessage()));

    }

    /**
     * Exception handler for ServiceRequestedNotFoundException.
     */
    @ExceptionHandler({ServiceTemplateNotRegistered.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateNotRegisteredException(
            ServiceTemplateNotRegistered ex) {
        return getErrorResponse(ErrorType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(ex.getMessage()));

    }

    /**
     * Exception handler for TerraformScriptFormatInvalidException.
     */
    @ExceptionHandler({TerraformScriptFormatInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleTerraformScriptFormatInvalidException(
            TerraformScriptFormatInvalidException ex) {
        return getErrorResponse(ErrorType.TERRAFORM_SCRIPT_INVALID, ex.getErrorReasons());
    }

    /**
     * Exception handler for ServiceTemplateUpdateNotAllowed.
     */
    @ExceptionHandler({ServiceTemplateUpdateNotAllowed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateUpdateNotAllowed(
            ServiceTemplateUpdateNotAllowed ex) {
        return getErrorResponse(ErrorType.SERVICE_TEMPLATE_UPDATE_NOT_ALLOWED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceTemplateUpdateNotAllowed.
     */
    @ExceptionHandler({InvalidValueSchemaException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidValueSchemaException(
            InvalidValueSchemaException ex) {
        return getErrorResponse(ErrorType.VARIABLE_SCHEMA_DEFINITION_INVALID,
                ex.getInvalidValueSchemaKeys());
    }

    /**
     * Exception handler for ServiceTemplateDisabledException.
     */
    @ExceptionHandler({ServiceTemplateDisabledException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateDisabledException(
            ServiceTemplateDisabledException ex) {
        return getErrorResponse(ErrorType.SERVICE_TEMPLATE_DISABLED,
                Collections.singletonList(ex.getMessage()));
    }


    /**
     * Exception handler for ServiceTemplateAlreadyReviewed.
     */
    @ExceptionHandler({ServiceTemplateAlreadyReviewed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateAlreadyReviewed(
            ServiceTemplateAlreadyReviewed ex) {
        return getErrorResponse(ErrorType.SERVICE_TEMPLATE_ALREADY_REVIEWED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for InvalidServiceVersionException.
     */
    @ExceptionHandler({InvalidServiceVersionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidServiceVersionException(
            InvalidServiceVersionException ex) {
        return getErrorResponse(ErrorType.INVALID_SERVICE_VERSION,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for InvalidServiceFlavorsException.
     */
    @ExceptionHandler({InvalidServiceFlavorsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidServiceFlavorsException(
            InvalidServiceFlavorsException ex) {
        return getErrorResponse(ErrorType.INVALID_SERVICE_FLAVORS, ex.getErrorReasons());
    }

    /**
     * Exception handler for UnavailableServiceRegionsException.
     */
    @ExceptionHandler({UnavailableServiceRegionsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleUnavailableServiceRegionsException(
            UnavailableServiceRegionsException ex) {
        return getErrorResponse(ErrorType.UNAVAILABLE_SERVICE_REGIONS, ex.getErrorReasons());
    }

    /**
     * Exception handler for InvalidBillingConfigException.
     */
    @ExceptionHandler({InvalidBillingConfigException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidBillingConfigException(
            InvalidBillingConfigException ex) {
        return getErrorResponse(ErrorType.INVALID_BILLING_CONFIG, ex.getErrorReasons());
    }

    /**
     * Exception handler for ServiceTemplateStillInUseException.
     */
    @ExceptionHandler({ServiceTemplateStillInUseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateStillInUseException(
            ServiceTemplateStillInUseException ex) {
        return getErrorResponse(ErrorType.SERVICE_TEMPLATE_STILL_IN_USE,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceTemplateChangeRequestNotFound.
     */
    @ExceptionHandler({ServiceTemplateChangeRequestNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceTemplateChangeRequestNotFoundException(
            ServiceTemplateChangeRequestNotFound ex) {
        return getErrorResponse(ErrorType.SERVICE_TEMPLATE_CHANGE_REQUEST_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }
}
