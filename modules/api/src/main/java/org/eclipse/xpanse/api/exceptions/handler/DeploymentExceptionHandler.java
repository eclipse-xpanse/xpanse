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
import org.eclipse.xpanse.modules.deployment.exceptions.DeploymentScriptsCreationFailedException;
import org.eclipse.xpanse.modules.models.billing.exceptions.ServicePriceCalculationFailed;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ActivitiTaskNotFoundException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.EulaNotAccepted;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.FileLockedException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.InvalidDeploymentVariableException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceDetailsNotAccessible;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceFlavorDowngradeNotAllowed;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.VariableValidationFailedException;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.models.service.statemanagement.exceptions.ServiceStateManagementTaskNotFound;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Exception handler related to deployment requests. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DeploymentExceptionHandler {

    /** Exception handler for FlavorInvalidException. */
    @ExceptionHandler({FlavorInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleFlavorInvalidException(FlavorInvalidException ex) {
        return getErrorResponse(
                ErrorType.FLAVOR_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for TerraformExecutorException. */
    @ExceptionHandler({TerraformExecutorException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ResponseBody
    public ErrorResponse handleTerraformExecutorException(TerraformExecutorException ex) {
        return getErrorResponse(
                ErrorType.TERRAFORM_EXECUTION_FAILED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for PluginNotFoundException. */
    @ExceptionHandler({PluginNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handlePluginNotFoundException(PluginNotFoundException ex) {
        return getErrorResponse(
                ErrorType.PLUGIN_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for DeployerNotFoundException. */
    @ExceptionHandler({DeployerNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleDeployerNotFoundException(DeployerNotFoundException ex) {
        return getErrorResponse(
                ErrorType.DEPLOYER_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for InvalidServiceStateException. */
    @ExceptionHandler({InvalidServiceStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidServiceStateException(InvalidServiceStateException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceNotDeployedException. */
    @ExceptionHandler({ServiceNotDeployedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceNotDeployedException(ServiceNotDeployedException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceNotDeployedException. */
    @ExceptionHandler({InvalidDeploymentVariableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidDeploymentVariableException(
            InvalidDeploymentVariableException ex) {
        return getErrorResponse(
                ErrorType.DEPLOYMENT_VARIABLE_INVALID, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for TerraformBootRequestFailedException. */
    @ExceptionHandler({TerraformBootRequestFailedException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ResponseBody
    public ErrorResponse handleTerraformBootRequestFailedException(
            TerraformBootRequestFailedException ex) {
        return getErrorResponse(
                ErrorType.TERRAFORM_BOOT_REQUEST_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for VariableValidationFailedException. */
    @ExceptionHandler({VariableValidationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleVariableValidationFailedException(
            VariableValidationFailedException ex) {
        return getErrorResponse(
                ErrorType.VARIABLE_VALIDATION_FAILED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceDetailsNotAccessible. */
    @ExceptionHandler({ServiceDetailsNotAccessible.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleServiceDetailsNotAccessible(ServiceDetailsNotAccessible ex) {
        return getErrorResponse(
                ErrorType.SERVICE_DETAILS_NOT_ACCESSIBLE,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ActivitiTaskNotFoundException. */
    @ExceptionHandler({ActivitiTaskNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleActivitiTaskNotFoundException(ActivitiTaskNotFoundException ex) {
        return getErrorResponse(
                ErrorType.ACTIVITI_TASK_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceLockedException. */
    @ExceptionHandler({ServiceLockedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceLockedException(ServiceLockedException ex) {
        return getErrorResponse(
                ErrorType.SERVICE_LOCKED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for EulaNotAccepted. */
    @ExceptionHandler({EulaNotAccepted.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleEulaNotAcceptedException(EulaNotAccepted ex) {
        return getErrorResponse(
                ErrorType.EULA_NOT_ACCEPTED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceFlavorDowngradeNotAllowed. */
    @ExceptionHandler({ServiceFlavorDowngradeNotAllowed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceFlavorDowngradeNotAllowed(
            ServiceFlavorDowngradeNotAllowed ex) {
        return getErrorResponse(
                ErrorType.SERVICE_FLAVOR_DOWNGRADE_NOT_ALLOWED,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for BillingModeNotSupported. */
    @ExceptionHandler({BillingModeNotSupported.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBillingModeNotSupported(BillingModeNotSupported ex) {
        return getErrorResponse(
                ErrorType.BILLING_MODE_NOT_SUPPORTED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceStateManagementTaskNotFound. */
    @ExceptionHandler({ServiceStateManagementTaskNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceStateManagementTaskNotFound(
            ServiceStateManagementTaskNotFound ex) {
        return getErrorResponse(
                ErrorType.SERVICE_STATE_MANAGEMENT_TASK_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServiceOrderNotFound. */
    @ExceptionHandler({ServiceOrderNotFound.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceOrderManagementTaskNotFound(ServiceOrderNotFound ex) {
        return getErrorResponse(
                ErrorType.SERVICE_ORDER_NOT_FOUND, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for ServicePriceCalculationFailed. */
    @ExceptionHandler({ServicePriceCalculationFailed.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServicePriceCalculationFailed(ServicePriceCalculationFailed ex) {
        return getErrorResponse(
                ErrorType.SERVICE_PRICE_CALCULATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for FileLockedException. */
    @ExceptionHandler({FileLockedException.class})
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ResponseBody
    public ErrorResponse handleFileLockedException(FileLockedException ex) {
        return getErrorResponse(ErrorType.FILE_LOCKED, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for InvalidDeployerToolException. */
    @ExceptionHandler({InvalidDeployerToolException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidDeployerToolException(InvalidDeployerToolException ex) {
        return getErrorResponse(
                ErrorType.INVALID_DEPLOYER_TOOL, Collections.singletonList(ex.getMessage()));
    }

    /** Exception handler for DeploymentScriptsCreationFailedException. */
    @ExceptionHandler({DeploymentScriptsCreationFailedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleDeploymentScriptsCreatedException(
            DeploymentScriptsCreationFailedException ex) {
        return getErrorResponse(
                ErrorType.DEPLOYMENT_SCRIPTS_CREATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }
}
