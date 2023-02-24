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
import org.eclipse.xpanse.modules.ocl.loader.data.models.v2.Oclv2;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin
public class OrchestratorApi {

    private final OrchestratorService orchestratorService;

    @Autowired
    public OrchestratorApi(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /**
     * Register new managed service.
     *
     * @param ocl object of managed service.
     * @return response
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response register(@Valid @RequestBody Ocl ocl) {
        log.info("Registering managed service with name {}", ocl.getName());
        this.orchestratorService.registerManagedService(ocl);
        String successMsg = String.format(
                "Managed service %s registered success.", ocl.getName());
        return Response.successResponse(successMsg);
    }

    /**
     * Register new managed service.
     *
     * @param ocl object of managed service.
     * @return response
     */
    @PostMapping(value = "/v2/register",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response register2(@Valid @RequestBody Oclv2 ocl) {
        log.info("Registering managed service with name {}", ocl.getName());
        String successMsg = String.format(
                "Managed service %s registered success.", ocl.getName());
        return Response.successResponse("success");
    }

    /**
     * Register managed service with URL.
     *
     * @param oclLocation URL of new Ocl.
     * @return response
     * @throws Exception exception
     */
    @PostMapping("/register/fetch")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response fetch(@RequestHeader(value = "ocl") String oclLocation) throws Exception {
        log.info("Registering managed service with Url {}", oclLocation);
        this.orchestratorService.registerManagedService(oclLocation);
        String successMsg = String.format(
                "Managed service registered with URL %s success.", oclLocation);
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

    /**
     * Get status of the managed service with name.
     *
     * @return Status of the managed service.
     */
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

    /**
     * Start registered managed service.
     *
     * @param managedServiceName name of managed service
     * @return response
     */
    @PostMapping("/start/{managedServiceName}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public Response start(@PathVariable("managedServiceName") String managedServiceName) {
        log.info("Starting managed service with name {}", managedServiceName);
        this.orchestratorService.startManagedService(managedServiceName);
        String successMsg = String.format(
                "Task of start managed service %s start running.", managedServiceName);
        return Response.successResponse(successMsg);
    }

    /**
     * Stop started managed service.
     *
     * @param managedServiceName name of managed service
     * @return response
     */
    @PostMapping("/stop/{managedServiceName}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public Response stop(@PathVariable("managedServiceName") String managedServiceName) {
        log.info("Stopping managed service with name {}", managedServiceName);
        this.orchestratorService.stopManagedService(managedServiceName);
        String successMsg = String.format(
                "Task of stop managed service %s start running.", managedServiceName);
        return Response.successResponse(successMsg);
    }

    /**
     * Update registered managed service.
     *
     * @param managedServiceName name of managed service
     * @return response
     */
    @PutMapping("/update/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response update(@PathVariable("managedServiceName") String managedServiceName,
            @RequestBody Ocl ocl) {
        log.info("Updating managed service with name {}", managedServiceName);
        this.orchestratorService.updateManagedService(managedServiceName, ocl);
        String successMsg = String.format(
                "Managed service %s updated success.", managedServiceName);
        return Response.successResponse(successMsg);
    }

    /**
     * Update registered managed service with URL.
     *
     * @param managedServiceName name of managed service.
     * @param oclLocation        new Ocl URL
     * @return response
     * @throws Exception exception
     */
    @PutMapping("/update/fetch/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response update(@PathVariable("managedServiceName") String managedServiceName,
            @RequestHeader(value = "ocl") String oclLocation) throws Exception {
        log.info("Updating managed service {} with url {}", managedServiceName, oclLocation);
        this.orchestratorService.updateManagedService(managedServiceName, oclLocation);
        String successMsg = String.format(
                "Managed service %s updated with URL %s success.", managedServiceName, oclLocation);
        return Response.successResponse(successMsg);
    }

    /**
     * Unregister registered managed service.
     *
     * @param managedServiceName name of managed service
     * @return response
     */
    @PostMapping("/unregister/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response unregister(@PathVariable("managedServiceName") String managedServiceName) {
        log.info("Unregistering managed service with name {}", managedServiceName);
        this.orchestratorService.unregisterManagedService(managedServiceName);
        String successMsg = String.format(
                "Managed service %s unregistered success.", managedServiceName);
        return Response.successResponse(successMsg);
    }

}
