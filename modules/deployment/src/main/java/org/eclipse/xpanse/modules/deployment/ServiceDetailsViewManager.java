/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.service.ServiceQueryModel;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceDetailsNotAccessible;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to manage all methods used for viewing deployed service details.
 */
@Slf4j
@Component
public class ServiceDetailsViewManager {

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource
    private ServiceStateManager serviceStateManager;
    @Resource
    private ServiceOrderManager serviceOrderManager;

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return serviceDetailVo
     */
    public DeployedServiceDetails getServiceDetailsByIdForIsv(UUID id) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        ServiceHostingType serviceHostingType =
                serviceDeploymentEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg = String.format("the details of Service with id %s no accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        boolean isManagedByCurrentUser = userServiceHelper
                .currentUserCanManageNamespace(serviceDeploymentEntity.getNamespace());
        if (!isManagedByCurrentUser) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        return EntityTransUtils.transToDeployedServiceDetails(serviceDeploymentEntity);
    }

    /**
     * List deploy services with query model.
     *
     * @param category       of the services to be filtered.
     * @param csp            of the services to be filtered.
     * @param serviceName    of the services to be filtered.
     * @param serviceVersion of the services to be filtered.
     * @param state          of the services to be filtered.
     * @return serviceVos
     */
    public List<DeployedService> listDeployedServices(Category category, Csp csp,
                                                      String serviceName, String serviceVersion,
                                                      ServiceDeploymentState state) {
        ServiceQueryModel query =
                getServiceQueryModel(category, csp, serviceName, serviceVersion, state);
        String currentUserId = userServiceHelper.getCurrentUserId();
        query.setUserId(currentUserId);
        return serviceDeploymentStorage.listServices(query).stream()
                .map(EntityTransUtils::convertToDeployedService).toList();
    }

    /**
     * List all deployed services details.
     *
     * @param category       of the services to be filtered.
     * @param csp            of the services to be filtered.
     * @param serviceName    of the services to be filtered.
     * @param serviceVersion of the services to be filtered.
     * @return serviceVos
     */
    public List<DeployedService> listDeployedServicesDetails(Category category, Csp csp,
                                                             String serviceName,
                                                             String serviceVersion,
                                                             ServiceDeploymentState serviceState) {
        List<DeployedService> servicesDetails = new ArrayList<>();
        List<DeployedService> services =
                listDeployedServices(category, csp, serviceName, serviceVersion, serviceState);
        if (!CollectionUtils.isEmpty(services)) {
            for (DeployedService deployService : services) {
                if (deployService.getServiceHostingType() == ServiceHostingType.SERVICE_VENDOR) {
                    servicesDetails.add(
                            getVendorHostedServiceDetailsByIdForEndUser(
                                    deployService.getServiceId()));
                } else {
                    servicesDetails.add(
                            getSelfHostedServiceDetailsByIdForEndUser(
                                    deployService.getServiceId()));
                }
            }
        }
        return servicesDetails;
    }

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return serviceDetailVo
     */
    public DeployedServiceDetails getSelfHostedServiceDetailsByIdForEndUser(UUID id) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(serviceDeploymentEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        ServiceHostingType serviceHostingType =
                serviceDeploymentEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SELF != serviceHostingType) {
            String errorMsg = String.format(
                    "details of non service-self hosted with id %s is not " + "accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        return EntityTransUtils.transToDeployedServiceDetails(serviceDeploymentEntity);
    }

    /**
     * Get vendor hosted service detail by id.
     *
     * @param id ID of deploy service.
     * @return VendorHostedDeployedServiceDetails
     */
    public VendorHostedDeployedServiceDetails getVendorHostedServiceDetailsByIdForEndUser(UUID id) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(serviceDeploymentEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        ServiceHostingType serviceHostingType =
                serviceDeploymentEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg = String.format(
                    "details of non service-vendor hosted with id %s is not accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        return EntityTransUtils.transToVendorHostedServiceDetails(serviceDeploymentEntity);
    }


    /**
     * Use query model to list SV deployment services.
     *
     * @param category       of the services to be filtered.
     * @param csp            of the services to be filtered.
     * @param serviceName    of the services to be filtered.
     * @param serviceVersion of the services to be filtered.
     * @param state          of the services to be filtered.
     * @return serviceVos
     */
    public List<DeployedService> listDeployedServicesOfIsv(Category category, Csp csp,
                                                           String serviceName,
                                                           String serviceVersion,
                                                           ServiceDeploymentState state) {

        ServiceQueryModel query =
                getServiceQueryModel(category, csp, serviceName, serviceVersion, state);
        String namespace = userServiceHelper.getCurrentUserManageNamespace();
        query.setNamespace(namespace);
        List<ServiceDeploymentEntity> deployServices = serviceDeploymentStorage.listServices(query);
        return deployServices.stream().map(EntityTransUtils::convertToDeployedService).toList();
    }

    private ServiceQueryModel getServiceQueryModel(Category category, Csp csp, String serviceName,
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


}
