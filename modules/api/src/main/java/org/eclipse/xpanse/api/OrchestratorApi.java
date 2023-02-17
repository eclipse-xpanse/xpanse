/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.response.Response;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.ServiceStatus;
import org.eclipse.xpanse.modules.ocl.loader.data.models.SystemStatus;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.HealthStatus;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST interface methods for processing OCL.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
public class OrchestratorApi {

    private final OrchestratorService orchestratorService;

    @Autowired
    public OrchestratorApi(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response register(@Valid @RequestBody Ocl ocl) {
        log.info("Registering managed service with name {}", ocl.getName());
        this.orchestratorService.registerManagedService(ocl);
        String successMsg = String.format("Managed service %s registered successfully",
            ocl.getName());
        return Response.successResponse(successMsg);
    }

    @PostMapping("/register/fetch")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response fetch(@RequestHeader(value = "ocl") String oclLocation) throws Exception {
        log.info("Registering managed service with Url {}", oclLocation);
        this.orchestratorService.registerManagedService(oclLocation);
        String successMsg = String.format("Managed service with URL %s registered successfully",
            oclLocation);
        return Response.successResponse(successMsg);
    }

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public SystemStatus health() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        return systemStatus;
    }

    @GetMapping("/services/state/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    public ServiceStatus state(@PathVariable("managedServiceName") String managedServiceName) {
        return this.orchestratorService.getManagedServiceState(managedServiceName);
    }

    /**
     * Profiles the names of the managed services currently deployed.
     *
     * @return list of all services deployed.
     */
    @GetMapping("/services")
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceStatus> services() {
        return this.orchestratorService.getStoredServices();
    }

    @PostMapping("/start/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response start(@PathVariable("managedServiceName") String managedServiceName) {
        log.info("Starting managed service with name {}", managedServiceName);
        this.orchestratorService.startManagedService(managedServiceName);
        String successMsg = String.format("Managed service %s starting successfully",
            managedServiceName);
        return Response.successResponse(successMsg);
    }

    @PostMapping("/stop/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response stop(@PathVariable("managedServiceName") String managedServiceName) {
        log.info("Stopping managed service with name {}", managedServiceName);
        this.orchestratorService.stopManagedService(managedServiceName);
        String successMsg = String.format("Managed service %s stopped successfully",
            managedServiceName);
        return Response.successResponse(successMsg);
    }

    @PutMapping("/update/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response update(@PathVariable("managedServiceName") String managedServiceName,
        @RequestBody Ocl ocl) {
        log.info("Updating managed service with name {}", managedServiceName);
        this.orchestratorService.updateManagedService(managedServiceName, ocl);
        String successMsg = String.format("Managed service %s updated successfully",
            managedServiceName);
        return Response.successResponse(successMsg);
    }

    @PutMapping("/update/fetch/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response update(@PathVariable("managedServiceName") String managedServiceName,
        @RequestHeader(value = "ocl") String oclLocation) throws Exception {
        log.info("Updating managed service {} with url {}", managedServiceName, oclLocation);
        this.orchestratorService.updateManagedService(managedServiceName, oclLocation);
        String successMsg = String.format("Managed service %s updated with url %s successfully",
            managedServiceName, oclLocation);
        return Response.successResponse(successMsg);
    }

}
