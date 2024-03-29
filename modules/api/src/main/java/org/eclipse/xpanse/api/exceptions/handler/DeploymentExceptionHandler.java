/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ActivitiTaskNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidDeploymentVariableException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceDetailsNotAccessible;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.VariableInvalidException;
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
    public Response handleFlavorInvalidException(
            FlavorInvalidException ex) {
        return Response.errorResponse(ResultType.FLAVOR_NOT_FOUND,
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
        return Response.errorResponse(ResultType.TERRAFORM_EXECUTION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for PluginNotFoundException.
     */
    @ExceptionHandler({PluginNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handlePluginNotFoundException(
            PluginNotFoundException ex) {
        return Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for DeployerNotFoundException.
     */
    @ExceptionHandler({DeployerNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleDeployerNotFoundException(
            DeployerNotFoundException ex) {
        return Response.errorResponse(ResultType.DEPLOYER_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for InvalidServiceStateException.
     */
    @ExceptionHandler({InvalidServiceStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleInvalidServiceStateException(
            InvalidServiceStateException ex) {
        return Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceNotDeployedException.
     */
    @ExceptionHandler({ServiceNotDeployedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleServiceNotDeployedException(
            ServiceNotDeployedException ex) {
        return Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
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
        return Response.errorResponse(ResultType.DEPLOYMENT_VARIABLE_INVALID,
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
        return Response.errorResponse(ResultType.TERRAFORM_BOOT_REQUEST_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for VariableInvalidException.
     */
    @ExceptionHandler({VariableInvalidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleVariableInvalidException(
            VariableInvalidException ex) {
        return Response.errorResponse(ResultType.VARIABLE_VALIDATION_FAILED,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ServiceDetailsNotAccessible.
     */
    @ExceptionHandler({ServiceDetailsNotAccessible.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Response handleServiceDetailsNotAccessible(
            ServiceDetailsNotAccessible ex) {
        return Response.errorResponse(ResultType.SERVICE_DETAILS_NOT_ACCESSIBLE,
                Collections.singletonList(ex.getMessage()));
    }

    /**
     * Exception handler for ActivitiTaskNotFoundException.
     */
    @ExceptionHandler({ActivitiTaskNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Response handleActivitiTaskNotFoundException(
            ActivitiTaskNotFoundException ex) {
        return Response.errorResponse(ResultType.ACTIVITI_TASK_NOT_FOUND,
                Collections.singletonList(ex.getMessage()));
    }
}
