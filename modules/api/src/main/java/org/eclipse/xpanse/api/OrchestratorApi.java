/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @Tag(name = "Service Vendor",
            description = "APIs to publish the managed services.")
    @PostMapping(value = "/register",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response register(@Valid @RequestBody Ocl ocl) {
        log.info("Registering managed service with name {}", ocl.getName());
        String successMsg = String.format(
                "Managed service %s registered success.", ocl.getName());
        return Response.successResponse(successMsg);
    }

    /**
     * Register managed service with URL.
     *
     * @param oclLocation URL of new Ocl.
     * @return response
     * @throws Exception exception
     */
    @Tag(name = "Service Vendor",
            description = "APIs to publish the managed services.")
    @PostMapping(value = "/register/file",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response fetch(@RequestParam(name = "oclLocation") String oclLocation) throws Exception {
        log.info("Registering managed service with Url {}", oclLocation);
        this.orchestratorService.registerManagedService(oclLocation);
        String successMsg = String.format(
                "Managed service registered with URL %s success.", oclLocation);
        return Response.successResponse(successMsg);
    }

    /**
     * Unregister registered managed service.
     *
     * @param managedServiceName name of managed service
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to publish the managed services.")
    @DeleteMapping("/register/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response unregister(@PathVariable("managedServiceName") String managedServiceName) {
        log.info("Unregistering managed service with name {}", managedServiceName);
        this.orchestratorService.unregisterManagedService(managedServiceName);
        String successMsg = String.format(
                "Managed service %s unregistered success.", managedServiceName);
        return Response.successResponse(successMsg);
    }

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @Tag(name = "Admin", description = "APIs to manage the service instances")
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
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @GetMapping("/service/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    public ServiceStatus state(@PathVariable("managedServiceName") String managedServiceName) {
        return this.orchestratorService.getManagedServiceState(managedServiceName);
    }

    /**
     * Profiles the names of the managed services currently deployed.
     *
     * @return list of all services deployed.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
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
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @PostMapping("/service/{managedServiceName}")
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
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @DeleteMapping("/service/{managedServiceName}")
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
    @Tag(name = "Service Vendor",
            description = "APIs to publish the managed services.")
    @PutMapping(value = "/register/{managedServiceName}",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
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
    @Tag(name = "Service Vendor",
            description = "APIs to publish the managed services.")
    @PutMapping(value = "/register/{managedServiceName}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response update(@PathVariable("managedServiceName") String managedServiceName,
            @RequestParam(name = "oclLocation") String oclLocation) throws Exception {
        log.info("Updating managed service {} with url {}", managedServiceName, oclLocation);
        this.orchestratorService.updateManagedService(managedServiceName, oclLocation);
        String successMsg = String.format(
                "Managed service %s updated with URL %s success.", managedServiceName, oclLocation);
        return Response.successResponse(successMsg);
    }


}
