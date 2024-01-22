/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.ServiceStateManager;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service Management REST API.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceStateManageApi {

    @Resource
    private ServiceStateManager serviceStateManager;

    /**
     * Start the service by the deployed service id.
     *
     * @param id service id.
     * @return deployedService.
     */
    @Tag(name = "Service Status Management", description = "APIs to manage the service instances")
    @Operation(description = "Start the service by the service id.")
    @PutMapping(value = "/services/start/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public DeployedService startService(@PathVariable("id") String id) {
        log.info("Start the service by service id : {}", id);
        return serviceStateManager.startService(UUID.fromString(id));
    }

    /**
     * Stop the service by the deployed service id.
     *
     * @param id service id.
     * @return deployedService.
     */
    @Tag(name = "Service Status Management", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to deploy service using registered service template.")
    @PutMapping(value = "/services/stop/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public DeployedService stopService(@PathVariable("id") String id) {
        return serviceStateManager.stopService(UUID.fromString(id));
    }

    /**
     * Restart the service by the deployed service id.
     *
     * @param id service id.
     * @return deployedService.
     */
    @Tag(name = "Service Status Management", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to deploy service using registered service template.")
    @PutMapping(value = "/services/restart/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public DeployedService restartService(@PathVariable("id") String id) {
        return serviceStateManager.restartService(UUID.fromString(id));
    }
}