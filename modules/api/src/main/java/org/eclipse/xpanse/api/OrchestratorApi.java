/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
    public void register(@Valid @RequestBody Ocl ocl) {
        log.info("Registering managed service with name {}", ocl.getName());
        this.orchestratorService.registerManagedService(ocl);
    }

    @PostMapping("/register/fetch")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void fetch(@RequestHeader(value = "ocl") String oclLocation) throws Exception {
        this.orchestratorService.registerManagedService(oclLocation);
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
    public void start(@PathVariable("managedServiceName") String managedServiceName) {
        this.orchestratorService.startManagedService(managedServiceName);
    }

    @PostMapping("/stop/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void stop(@PathVariable("managedServiceName") String managedServiceName) {
        this.orchestratorService.stopManagedService(managedServiceName);
    }

    @PutMapping("/update/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("managedServiceName") String managedServiceName,
            @RequestBody Ocl ocl) {
        this.orchestratorService.updateManagedService(managedServiceName, ocl);
    }

    @PutMapping("/update/fetch/{managedServiceName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("managedServiceName") String managedServiceName,
            @RequestHeader(value = "ocl") String oclLocation) throws Exception {
        this.orchestratorService.updateManagedService(managedServiceName, oclLocation);
    }

}
