/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.service.ServiceQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceDetailsNotAccessible;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage all methods used for viewing deployed service details. */
@Slf4j
@Component
public class ServiceDetailsViewManager {

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @Resource private UserServiceHelper userServiceHelper;
    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource private ServiceTemplateStorage serviceTemplateStorage;
    @Resource private ServiceResultReFetchManager serviceResultReFetchManager;

    /**
     * Get deploy service detail by id.
     *
     * @param id ID of deploy service.
     * @return serviceDetailVo
     */
    public DeployedServiceDetails getServiceDetailsByIdForIsv(UUID id) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(id);
        ServiceHostingType serviceHostingType =
                serviceDeploymentEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg = String.format("the details of Service with id %s no accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        boolean isManagedByCurrentUser =
                userServiceHelper.currentUserCanManageIsv(
                        serviceDeploymentEntity.getServiceVendor());
        if (!isManagedByCurrentUser) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other service vendors",
                            UserOperation.VIEW_DETAILS_OF_SERVICE.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
        serviceResultReFetchManager.reFetchDeploymentStateForMissingOrdersFromDeployers(
                serviceDeploymentEntity);
        return EntityTransUtils.transToDeployedServiceDetails(serviceDeploymentEntity);
    }

    /**
     * List deploy services with query model.
     *
     * @param category of the services to be filtered.
     * @param csp of the services to be filtered.
     * @param serviceName of the services to be filtered.
     * @param serviceVersion of the services to be filtered.
     * @param state of the services to be filtered.
     * @return serviceVos
     */
    public List<DeployedService> listDeployedServices(
            Category category,
            Csp csp,
            String serviceName,
            String serviceVersion,
            ServiceDeploymentState state) {
        ServiceQueryModel query =
                getServiceQueryModel(category, csp, serviceName, serviceVersion, state);
        String currentUserId = userServiceHelper.getCurrentUserId();
        query.setUserId(currentUserId);
        List<ServiceDeploymentEntity> serviceDeploymentEntities =
                serviceDeploymentStorage.listServices(query);
        serviceDeploymentEntities.forEach(
                serviceDeployment ->
                        serviceResultReFetchManager
                                .reFetchDeploymentStateForMissingOrdersFromDeployers(
                                        serviceDeployment));
        return setServiceConfigurationForDeployedServiceList(serviceDeploymentEntities);
    }

    /**
     * List all deployed services details.
     *
     * @param category of the services to be filtered.
     * @param csp of the services to be filtered.
     * @param serviceName of the services to be filtered.
     * @param serviceVersion of the services to be filtered.
     * @return serviceVos
     */
    public List<DeployedService> listDeployedServicesDetails(
            Category category,
            Csp csp,
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
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(id);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(serviceDeploymentEntity.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.",
                            UserOperation.VIEW_DETAILS_OF_SERVICE.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
        ServiceHostingType serviceHostingType =
                serviceDeploymentEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SELF != serviceHostingType) {
            String errorMsg =
                    String.format(
                            "details of non service-self hosted with id %s is not accessible", id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        serviceResultReFetchManager.reFetchDeploymentStateForMissingOrdersFromDeployers(
                serviceDeploymentEntity);
        DeployedServiceDetails details =
                EntityTransUtils.transToDeployedServiceDetails(serviceDeploymentEntity);
        setServiceConfigurationDetailsForDeployedService(details);
        return details;
    }

    /**
     * Get vendor hosted service detail by id.
     *
     * @param id ID of deploy service.
     * @return VendorHostedDeployedServiceDetails
     */
    public VendorHostedDeployedServiceDetails getVendorHostedServiceDetailsByIdForEndUser(UUID id) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(id);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(serviceDeploymentEntity.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.",
                            UserOperation.VIEW_DETAILS_OF_SERVICE.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
        ServiceHostingType serviceHostingType =
                serviceDeploymentEntity.getDeployRequest().getServiceHostingType();
        if (ServiceHostingType.SERVICE_VENDOR != serviceHostingType) {
            String errorMsg =
                    String.format(
                            "details of non service-vendor hosted with id %s is not accessible",
                            id);
            log.error(errorMsg);
            throw new ServiceDetailsNotAccessible(errorMsg);
        }
        VendorHostedDeployedServiceDetails details =
                EntityTransUtils.transToVendorHostedServiceDetails(serviceDeploymentEntity);
        setServiceConfigurationDetailsForDeployedService(details);
        return details;
    }

    /**
     * Use query model to list ISV deployment services.
     *
     * @param category of the services to be filtered.
     * @param serviceName of the services to be filtered.
     * @param serviceVersion of the services to be filtered.
     * @param state of the services to be filtered.
     * @return serviceVos
     */
    public List<DeployedService> getAllDeployedServicesByIsv(
            Category category,
            Csp csp,
            String serviceName,
            String serviceVersion,
            ServiceDeploymentState state) {

        ServiceQueryModel query =
                getServiceQueryModel(category, csp, serviceName, serviceVersion, state);
        String isv = userServiceHelper.getCurrentUserManageIsv();
        query.setServiceVendor(isv);
        List<ServiceDeploymentEntity> deployServices = serviceDeploymentStorage.listServices(query);
        deployServices.forEach(
                serviceDeployment ->
                        serviceResultReFetchManager
                                .reFetchDeploymentStateForMissingOrdersFromDeployers(
                                        serviceDeployment));
        return setServiceConfigurationForDeployedServiceList(deployServices);
    }

    /**
     * Use query model to list CSP deployment services.
     *
     * @param category of the services to be filtered.
     * @param serviceName of the services to be filtered.
     * @param serviceVersion of the services to be filtered.
     * @param state of the services to be filtered.
     * @return serviceVos
     */
    public List<DeployedService> getAllDeployedServicesByCsp(
            Category category,
            String serviceName,
            String serviceVersion,
            ServiceDeploymentState state) {
        Csp csp = userServiceHelper.getCurrentUserManageCsp();
        ServiceQueryModel query =
                getServiceQueryModel(category, csp, serviceName, serviceVersion, state);
        List<ServiceDeploymentEntity> deployServices = serviceDeploymentStorage.listServices(query);
        deployServices.forEach(
                serviceDeployment ->
                        serviceResultReFetchManager
                                .reFetchDeploymentStateForMissingOrdersFromDeployers(
                                        serviceDeployment));
        return setServiceConfigurationForDeployedServiceList(deployServices);
    }

    private List<DeployedService> setServiceConfigurationForDeployedServiceList(
            List<ServiceDeploymentEntity> deployServices) {
        return deployServices.stream()
                .map(
                        serviceDeployment -> {
                            DeployedService deployedService =
                                    EntityTransUtils.convertToDeployedService(serviceDeployment);
                            if (Objects.nonNull(deployedService)) {
                                setServiceConfigurationDetailsForDeployedService(deployedService);
                            }
                            return deployedService;
                        })
                .toList();
    }

    private void setServiceConfigurationDetailsForDeployedService(DeployedService deployedService) {
        ServiceTemplateEntity serviceTemplate =
                serviceTemplateStorage.getServiceTemplateById(
                        deployedService.getServiceTemplateId());
        if (Objects.nonNull(serviceTemplate)
                && Objects.nonNull(serviceTemplate.getOcl().getServiceConfigurationManage())) {
            List<ServiceChangeParameter> configurationParameters =
                    serviceTemplate
                            .getOcl()
                            .getServiceConfigurationManage()
                            .getConfigurationParameters();
            Map<String, Object> configuration =
                    Objects.isNull(deployedService.getServiceConfigurationDetails())
                            ? null
                            : deployedService.getServiceConfigurationDetails().getConfiguration();
            ServiceConfigurationDetails details =
                    mergeConfigurationParametersFromTemplate(
                            configurationParameters,
                            configuration,
                            serviceTemplate.getLastModifiedTime());
            deployedService.setServiceConfigurationDetails(details);
        }
    }

    /**
     * Method to dynamically merge and generate the current configuration of a service in case the
     * service configuration parameters have been updated in the template by the ISV after a service
     * was deployed.
     */
    private ServiceConfigurationDetails mergeConfigurationParametersFromTemplate(
            List<ServiceChangeParameter> parameters,
            Map<String, Object> configuration,
            OffsetDateTime updateTime) {
        Map<String, Object> configurationParameterMap = new HashMap<>();
        parameters.forEach(
                configurationParameter -> {
                    configurationParameterMap.put(
                            configurationParameter.getName(),
                            configurationParameter.getInitialValue());
                });
        if (!CollectionUtils.isEmpty(configuration)) {
            configurationParameterMap.forEach(
                    (k, v) -> {
                        if (configuration.containsKey(k)) {
                            // override the value with the configuration table value.
                            configurationParameterMap.put(k, configuration.get(k));
                        }
                    });
        }
        ServiceConfigurationDetails details = new ServiceConfigurationDetails();
        details.setConfiguration(configurationParameterMap);
        details.setUpdatedTime(updateTime);
        return details;
    }

    private ServiceQueryModel getServiceQueryModel(
            Category category,
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
        return query;
    }
}
