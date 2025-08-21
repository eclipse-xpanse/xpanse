/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.tools;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.ai.generate.ApplicationGenerationManager;
import org.eclipse.xpanse.ai.template.ServiceTemplateGenerator;
import org.eclipse.xpanse.api.controllers.ServiceCatalogApi;
import org.eclipse.xpanse.api.controllers.ServiceDeployerApi;
import org.eclipse.xpanse.modules.models.ai.enums.AiApplicationType;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.SimpleDeployRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions.ServiceTemplateRequestNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

/** Bean to configure services as MCP tools. */
@Component
@Slf4j
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
@Profile("ai")
public class McpTools {

    @Resource private ServiceCatalogApi serviceCatalogApi;

    @Resource private ServiceDeployerApi serviceDeployerApi;

    @Resource private ApplicationGenerationManager applicationGenerationManager;
    @Resource private ServiceTemplateGenerator serviceTemplateGenerator;

    /**
     * Get deployable service by id.
     *
     * @param serviceTemplateId The id of orderable service.
     * @return userOrderableServiceVo
     */
    @Tool(description = "Get deployable service details by id.")
    public UserOrderableServiceVo getOrderableServiceDetailsById(
            @ToolParam(description = "The id of orderable service.")
                    @Parameter(
                            name = "serviceTemplateId",
                            description = "The id of orderable service.")
                    @PathVariable("serviceTemplateId")
                    String serviceTemplateId) {
        return serviceCatalogApi.getServiceTemplateDetailsById(UUID.fromString(serviceTemplateId));
    }

    /**
     * List all approved service templates which are available for user to order/deploy.
     *
     * @param categoryName category of the service.
     * @param cspName name of the cloud service provider.
     * @return service templates
     */
    @Tool(description = "List of all approved services which are available for user to order.")
    public List<UserOrderableServiceVo> getOrderableServices(
            @ToolParam(description = "category of the service") Category categoryName,
            @ToolParam(description = "name of the cloud service provider") Csp cspName) {
        return serviceCatalogApi.getAllUserOrderableServices(
                categoryName, cspName, null, null, null);
    }

    /**
     * Create an order task to deploy new service using approved service template.
     *
     * @param simpleDeployRequest the request to deploy new service.
     * @return ServiceOrder
     */
    @Tool(
            description =
                    "Create an order task to deploy new service using approved service template."
                        + " All mandatory properties of the service must be requested by the user"
                        + " before calling this tool.")
    public ServiceOrder simpleDeploy(
            @ToolParam SimpleDeployRequest simpleDeployRequest,
            @ToolParam(
                            description =
                                    "Ask user if you can proceed with calling this tool. THe input"
                                            + " for this parameter must come from user. If the user"
                                            + " says 'YES' then set it to true otherwise set it to"
                                            + " false")
                    boolean isUserAccepted) {
        if (!isUserAccepted) {
            throw new ServiceTemplateRequestNotAllowed("User confirmation not provided.");
        }
        UserOrderableServiceVo userOrderableServiceVo =
                serviceCatalogApi.getServiceTemplateDetailsById(
                        simpleDeployRequest.getServiceTemplateId());
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setCategory(userOrderableServiceVo.getCategory());
        deployRequest.setCsp(userOrderableServiceVo.getCsp());
        deployRequest.setServiceHostingType(userOrderableServiceVo.getServiceHostingType());
        deployRequest.setBillingMode(userOrderableServiceVo.getBilling().getDefaultBillingMode());
        deployRequest.setFlavor(
                userOrderableServiceVo.getFlavors().getServiceFlavors().getFirst().getName());
        deployRequest.setEulaAccepted(true);
        deployRequest.setRegion(userOrderableServiceVo.getRegions().getFirst());
        deployRequest.setServiceName(userOrderableServiceVo.getName());
        deployRequest.setVersion(userOrderableServiceVo.getVersion());
        deployRequest.setServiceRequestProperties(
                simpleDeployRequest.getServiceRequestProperties());
        return this.serviceDeployerApi.deployService(deployRequest);
    }

    /**
     * List all deployed services details.
     *
     * @return list of all services deployed by a user.
     */
    @Tool(description = "List details of deployed services using parameters.")
    public List<DeployedService> getAllDeployedServicesWithDetails() {
        // return type is DeployedService but actually returns one of the child types
        // VendorHostedDeployedServiceDetails or DeployedServiceDetails
        return this.serviceDeployerApi.getDeployedServiceDetails(null, null, null, null, null);
    }

    /**
     * Generates a non-existing AI application and adds it to the catalog. This method creates an AI
     * service that is not currently available in the catalog. The types of supported services are
     * defined by the AiApplicationType enum. The method requires explicit user confirmation before
     * proceeding, as it is a potentially dangerous operation. If the user does not accept, an
     * exception is thrown.
     *
     * @param aiApplicationType The type of AI application to be created.
     * @param isUserAccepted Boolean indicating whether the user has confirmed the operation.
     * @return ServiceTemplateRequestInfo containing details of the registered service template.
     * @throws Exception If an error occurs during generation or registration.
     * @throws ServiceTemplateRequestNotAllowed If user confirmation is not provided.
     */
    @Tool(
            description =
                    "Adds a non existing AI service to catalog. The types of supported service is"
                        + " defined by the AiApplicationType enum. WARNING - This is a DANGEROUS"
                        + " SERVICE. This tool must not be invoked without the user confirming the"
                        + " AI Application type he needs to add. NEVER call this service until user"
                        + " asks to create a non existing service.")
    public ServiceTemplateRequestInfo generateNonExistingApplicationAndAddItToCatalog(
            @ToolParam(description = "Type of AI application to be created")
                    AiApplicationType aiApplicationType,
            @ToolParam(
                            description =
                                    "Ask user if you can proceed with calling this tool. THe input"
                                            + " for this parameter must come from user. If the user"
                                            + " says 'YES' then set it to true otherwise set it to"
                                            + " false")
                    boolean isUserAccepted) {
        if (!isUserAccepted) {
            throw new ServiceTemplateRequestNotAllowed("User confirmation not provided.");
        }
        String imageUrl = null;
        try {
            imageUrl =
                    applicationGenerationManager.generateApplicationServerImage(aiApplicationType);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new XpanseUnhandledException(e.getMessage());
        }
        return serviceTemplateGenerator.generateServiceTemplate(aiApplicationType, imageUrl);
    }
}
