/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler.getErrorResponse;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.billing.exceptions.ServicePriceCalculationFailed;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ActivitiTaskNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.EulaNotAccepted;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FileLockedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidDeploymentVariableException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceDetailsNotAccessible;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceFlavorDowngradeNotAllowed;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.VariableValidationFailedException;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.models.service.statemanagement.exceptions.ServiceStateManagementTaskNotFound;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler related to deployment requests.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DeploymentExceptionHandler {

    /**
     * Exception handler for FlavorInvalidException.
     */
    @ExceptionHandler({FlavorInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleFlavorInvalidException(FlavorInvalidException ex) {
        return getErrorResponse(ResultType.FLAVOR_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for TerraformExecutorException.
     */
    @ExceptionHandler({TerraformExecutorException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ResponseBody
    public Response handleTerraformExecutorException(
            TerraformExecutorException ex) {
        return getErrorResponse(ResultType.TERRAFORM_EXECUTION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for PluginNotFoundException.
     */
    @ExceptionHandler({PluginNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handlePluginNotFoundException(PluginNotFoundException ex) {
        return getErrorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for DeployerNotFoundException.
     */
    @ExceptionHandler({DeployerNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleDeployerNotFoundException(DeployerNotFoundException ex) {
        return getErrorResponse(ResultType.DEPLOYER_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for InvalidServiceStateException.
     */
    @ExceptionHandler({InvalidServiceStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleInvalidServiceStateException(InvalidServiceStateException ex) {
        return getErrorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceNotDeployedException.
     */
    @ExceptionHandler({ServiceNotDeployedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceNotDeployedException(ServiceNotDeployedException ex) {
        return getErrorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceNotDeployedException.
     */
    @ExceptionHandler({InvalidDeploymentVariableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleInvalidDeploymentVariableException(
            InvalidDeploymentVariableException ex) {
        return getErrorResponse(ResultType.DEPLOYMENT_VARIABLE_INVALID,
                Collections.singletonList(ex.getMessage()));
    }


    /**
     * Exception handler for TerraformBootRequestFailedException.
     */
    @ExceptionHandler({TerraformBootRequestFailedException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ResponseBody
    public Response handleTerraformBootRequestFailedException(
            TerraformBootRequestFailedException ex) {
        return getErrorResponse(ResultType.TERRAFORM_BOOT_REQUEST_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for VariableValidationFailedException.
     */
    @ExceptionHandler({VariableValidationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleVariableValidationFailedException(VariableValidationFailedException ex) {
        return getErrorResponse(ResultType.VARIABLE_VALIDATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceDetailsNotAccessible.
     */
    @ExceptionHandler({ServiceDetailsNotAccessible.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Response handleServiceDetailsNotAccessible(ServiceDetailsNotAccessible ex) {
        return getErrorResponse(ResultType.SERVICE_DETAILS_NOT_ACCESSIBLE,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ActivitiTaskNotFoundException.
     */
    @ExceptionHandler({ActivitiTaskNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleActivitiTaskNotFoundException(ActivitiTaskNotFoundException ex) {
        return getErrorResponse(ResultType.ACTIVITI_TASK_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceLockedException.
     */
    @ExceptionHandler({ServiceLockedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceLockedException(ServiceLockedException ex) {
        return getErrorResponse(ResultType.SERVICE_LOCKED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for EulaNotAccepted.
     */
    @ExceptionHandler({EulaNotAccepted.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleEulaNotAcceptedException(EulaNotAccepted ex) {
        return getErrorResponse(ResultType.EULA_NOT_ACCEPTED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceFlavorDowngradeNotAllowed.
     */
    @ExceptionHandler({ServiceFlavorDowngradeNotAllowed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceFlavorDowngradeNotAllowed(ServiceFlavorDowngradeNotAllowed ex) {
        return getErrorResponse(ResultType.SERVICE_FLAVOR_DOWNGRADE_NOT_ALLOWED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for BillingModeNotSupported.
     */
    @ExceptionHandler({BillingModeNotSupported.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleBillingModeNotSupported(BillingModeNotSupported ex) {
        return getErrorResponse(ResultType.BILLING_MODE_NOT_SUPPORTED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceStateManagementTaskNotFound.
     */
    @ExceptionHandler({ServiceStateManagementTaskNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceStateManagementTaskNotFound(
            ServiceStateManagementTaskNotFound ex) {
        return getErrorResponse(ResultType.SERVICE_STATE_MANAGEMENT_TASK_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceOrderNotFound.
     */
    @ExceptionHandler({ServiceOrderNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceOrderManagementTaskNotFound(ServiceOrderNotFound ex) {
        return getErrorResponse(ResultType.SERVICE_ORDER_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServicePriceCalculationFailed.
     */
    @ExceptionHandler({ServicePriceCalculationFailed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServicePriceCalculationFailed(ServicePriceCalculationFailed ex) {
        return getErrorResponse(ResultType.SERVICE_PRICE_CALCULATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for FileLockedException.
     */
    @ExceptionHandler({FileLockedException.class})
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ResponseBody
    public Response handleFileLockedException(FileLockedException ex) {
        return getErrorResponse(ResultType.FILE_LOCKED, Collections.singletonList(ex.getMessage()));
    }
}
