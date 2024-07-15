/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ISV;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceDetailsViewManager;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@Secured({ROLE_ISV})
public class IsvServiceDeployApi {

    @Resource
    private ServiceDetailsViewManager serviceDetailsViewManager;

    /**
     * List all deployed services by a user of ISV.
     *
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "List all deployed services by a user.")
    @GetMapping(value = "/services/isv",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public List<DeployedService> listDeployedServicesOfIsv(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) Category category,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName", required = false) Csp csp,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion,
            @Parameter(name = "serviceState", description = "deployment state of the service")
            @RequestParam(name = "serviceState", required = false)
            ServiceDeploymentState serviceState) {
        return this.serviceDetailsViewManager.listDeployedServicesOfIsv(
                category, csp, serviceName, serviceVersion, serviceState);
    }

    /**
     * Get details of the managed service by serviceId for ISV role.
     *
     * @return Details of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Get the details of the deployed service by service id.")
    @GetMapping(value = "/services/isv/details/vendor_hosted/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public DeployedServiceDetails getServiceDetailsByIdForIsv(
            @Parameter(name = "serviceId", description = "Id of deployed service")
            @PathVariable("serviceId") String serviceId) {
        return this.serviceDetailsViewManager.getServiceDetailsByIdForIsv(
                UUID.fromString(serviceId));
    }
}
