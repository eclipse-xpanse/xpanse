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
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.ServiceQueryModel;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.UserNotLoggedInException;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceDetailsNotAccessible;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.springframework.beans.BeanUtils;
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
    private IdentityProviderManager identityProviderManager;

    @Resource
    private DeployServiceStorage deployServiceStorage;

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return serviceDetailVo
     */
    public DeployedServiceDetails getServiceDetailsByIdForIsv(UUID id) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        ServiceHostingType serviceHostingType =
                deployServiceEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg = String.format("the details of Service with id %s no accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        if (namespace.isEmpty() || !namespace.get().equals(deployServiceEntity.getNamespace())) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        return EntityTransUtils.transDeployServiceEntityToServiceDetailVo(deployServiceEntity);
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
    public List<DeployedService> listDeployedServices(Category category,
                                                      Csp csp,
                                                      String serviceName,
                                                      String serviceVersion,
                                                      ServiceDeploymentState state) {
        ServiceQueryModel query = getServiceQueryModel(
                category, csp, serviceName, serviceVersion, state);
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        query.setUserId(userIdOptional.orElse(null));
        List<DeployServiceEntity> deployServices =
                deployServiceStorage.listServices(query);
        return deployServices.stream()
                .map(this::convertToDeployedService).toList();

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
            String serviceName, String serviceVersion, ServiceDeploymentState serviceState) {
        List<DeployedService> servicesDetails = new ArrayList<>();
        List<DeployedService> services =
                listDeployedServices(category, csp, serviceName, serviceVersion, serviceState);
        if (!CollectionUtils.isEmpty(services)) {
            for (DeployedService deployService : services) {
                if (deployService.getServiceHostingType() == ServiceHostingType.SERVICE_VENDOR) {
                    servicesDetails.add(
                            getVendorHostedServiceDetailsByIdForEndUser(deployService.getId()));
                } else {
                    servicesDetails.add(
                            getSelfHostedServiceDetailsByIdForEndUser(deployService.getId()));
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
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        ServiceHostingType serviceHostingType =
                deployServiceEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SELF != serviceHostingType) {
            String errorMsg = String.format("details of non service-self hosted with id %s is not "
                    + "accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        return EntityTransUtils.transDeployServiceEntityToServiceDetailVo(deployServiceEntity);
    }

    /**
     * Get vendor hosted service detail by id.
     *
     * @param id ID of deploy service.
     * @return VendorHostedDeployedServiceDetails
     */
    public VendorHostedDeployedServiceDetails getVendorHostedServiceDetailsByIdForEndUser(UUID id) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(id);
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view details of services belonging to other users.");
        }
        ServiceHostingType serviceHostingType =
                deployServiceEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg = String.format(
                    "details of non service-vendor hosted with id %s is not accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        return EntityTransUtils.transServiceEntityToVendorHostedServiceDetailsVo(
                deployServiceEntity);
    }

    private DeployedService convertToDeployedService(DeployServiceEntity serviceEntity) {
        if (Objects.nonNull(serviceEntity)) {
            DeployedService deployedService = new DeployedService();
            BeanUtils.copyProperties(serviceEntity, deployedService);
            deployedService.setServiceHostingType(
                    serviceEntity.getDeployRequest().getServiceHostingType());
            return deployedService;
        }
        return null;
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
    public List<DeployedService> listDeployedServicesOfIsv(Category category,
                                                           Csp csp,
                                                           String serviceName,
                                                           String serviceVersion,
                                                           ServiceDeploymentState state) {
        ServiceQueryModel query = getServiceQueryModel(
                category, csp, serviceName, serviceVersion, state);
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        return namespace.map(s -> deployServiceStorage.listServices(query).stream()
                .filter(deployServiceEntity -> s
                        .equals(deployServiceEntity.getNamespace()))
                .map(this::convertToDeployedService).toList()).orElseGet(ArrayList::new);
    }

    private ServiceQueryModel getServiceQueryModel(Category category,
                                                   Csp csp,
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
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (userIdOptional.isEmpty()) {
            throw new UserNotLoggedInException("Unable to get current login information");
        }
        query.setUserId(userIdOptional.get());
        return query;
    }


}
