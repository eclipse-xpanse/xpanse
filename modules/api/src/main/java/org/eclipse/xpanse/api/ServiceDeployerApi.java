/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_USER;

import io.nflow.engine.service.WorkflowInstanceService;
import io.nflow.engine.workflow.instance.WorkflowInstance;
import io.nflow.engine.workflow.instance.WorkflowInstanceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.query.ServiceQueryModel;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.eclipse.xpanse.modules.nflow.definition.ServiceMigrationWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceDeployerApi {

    private final DeployService deployService;

    private final WorkflowInstanceService workflowInstances;

    private final WorkflowInstanceFactory workflowInstanceFactory;

    @Autowired
    public ServiceDeployerApi(DeployService deployService,
            WorkflowInstanceService workflowInstances,
            WorkflowInstanceFactory workflowInstanceFactory) {
        this.deployService = deployService;
        this.workflowInstances = workflowInstances;
        this.workflowInstanceFactory = workflowInstanceFactory;
    }

    /**
     * Get details of the managed service by id.
     *
     * @return Details of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Get deployed service details by id.")
    @GetMapping(value = "/services/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ServiceDetailVo getServiceDetailsById(
            @Parameter(name = "id", description = "Task id of deployed service")
            @PathVariable("id") String id) {

        return this.deployService.getDeployServiceDetails(UUID.fromString(id));
    }

    /**
     * List all deployed services by a user.
     *
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "List all deployed services by a user.")
    @GetMapping(value = "/services",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceVo> listDeployedServices(
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
        ServiceQueryModel query =
                getServiceQueryModel(category, csp, serviceName, serviceVersion, serviceState);
        return this.deployService.listDeployedServices(query);
    }

    /**
     * Start a task to deploy registered service.
     *
     * @param deployRequest the managed service to create.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to deploy service using registered service template.")
    @PostMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID deploy(@Valid @RequestBody CreateRequest deployRequest) {
        log.info("Starting managed service with name {}, version {}, csp {}",
                deployRequest.getServiceName(),
                deployRequest.getVersion(), deployRequest.getCsp());
        UUID id = UUID.randomUUID();
        if (StringUtils.isBlank(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        DeployTask deployTask = new DeployTask();
        deployRequest.setId(id);
        deployTask.setId(id);
        deployTask.setCreateRequest(deployRequest);
        Deployment deployment = this.deployService.getDeployHandler(deployTask);
        this.deployService.asyncDeployService(deployment, deployTask);
        String successMsg = String.format(
                "Task of start managed service %s-%s-%s start running. UUID %s",
                deployRequest.getServiceName(),
                deployRequest.getVersion(), deployRequest.getCsp(), deployTask.getId());
        log.info(successMsg);
        return id;
    }

    /**
     * Start a task to destroy the deployed service using id.
     *
     * @param id ID of deployed service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to destroy the deployed service using id.")
    @DeleteMapping(value = "/services/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Response destroy(@PathVariable("id") String id) {
        log.info("Stopping managed service with id {}", id);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString(id));
        Deployment deployment = this.deployService.getDestroyHandler(deployTask);
        this.deployService.asyncDestroyService(deployment, deployTask);
        String successMsg = String.format(
                "Task of stop managed service %s start running.", id);
        return Response.successResponse(Collections.singletonList(successMsg));
    }

    /**
     * Start a task to purge the deployed service using id.
     *
     * @param id ID of deployed service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to purge the deployed service using id.")
    @DeleteMapping(value = "/services/purge/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Response purge(@PathVariable("id") String id) {
        log.info("Purging managed service with id {}", id);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString(id));
        Deployment deployment = this.deployService.getDestroyHandler(deployTask);
        this.deployService.purgeService(deployment, deployTask);
        String successMsg = String.format("Purging task for service with ID %s has started.", id);
        return Response.successResponse(Collections.singletonList(successMsg));
    }

    /**
     * Start a task to migrate the deployed service using id
     *
     * @param deployRequest the managed service to create.
     * @param oldId            ID of deployed service
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to migrate the deployed service using id.")
    @PostMapping(value = "/services/migrate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID migrate(@Valid @RequestBody CreateRequest deployRequest,
            @PathVariable("id") String oldId) {
        log.info("Migrate managed service with id {}", oldId);
        UUID newId = UUID.randomUUID();
        workflowInstances.insertWorkflowInstance(
                getWorkflowInstance(oldId, deployRequest, newId));
        return newId;
    }

    private WorkflowInstance getWorkflowInstance(String oldId,
            CreateRequest deployRequest, UUID newId) {
        return workflowInstanceFactory.newWorkflowInstanceBuilder()
                .setType(ServiceMigrationWorkflow.TYPE)
                .putStateVariable(ServiceMigrationWorkflow.NEW_ID, newId)
                .putStateVariable(ServiceMigrationWorkflow.DEPLOY_REQUEST, deployRequest)
                .putStateVariable(ServiceMigrationWorkflow.OLD_ID, oldId)
                .setBusinessKey(newId.toString())
                .setExternalId(newId.toString())
                .build();
    }

    private ServiceQueryModel getServiceQueryModel(Category category, Csp csp,
                                                   String serviceName,
                                                   String serviceVersion,
                                                   ServiceDeploymentState state) {
        ServiceQueryModel query = new ServiceQueryModel();
        if (Objects.nonNull(category)) {
            query.setCategory(category);
        }
        if (Objects.nonNull(csp)) {
            query.setCsp(csp);
        }
        if (StringUtils.isNotBlank(serviceName)) {
            query.setServiceName(serviceName);
        }
        if (StringUtils.isNotBlank(serviceVersion)) {
            query.setServiceVersion(serviceVersion);
        }
        if (Objects.nonNull(state)) {
            query.setServiceState(state);
        }
        return query;
    }

    private String generateCustomerServiceName(CreateRequest createRequest) {
        if (createRequest.getServiceName().length() > 5) {
            return createRequest.getServiceName().substring(0, 4) + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        } else {
            return createRequest.getServiceName() + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        }
    }
}
