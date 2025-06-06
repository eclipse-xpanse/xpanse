/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.api.controllers.ServiceCatalogApi;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.servicetemplate.EndUserFlavors;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.OutputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.DeploymentVariableHelper;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

/** ServiceTemplateEntityConverter. */
public class ServiceTemplateEntityConverter {

    /**
     * Convert ServiceTemplateEntity to ServiceTemplateDetailVo.
     *
     * @param serviceTemplateEntity serviceTemplateEntity
     * @return serviceTemplateDetailVo
     */
    public static ServiceTemplateDetailVo convertToServiceTemplateDetailVo(
            ServiceTemplateEntity serviceTemplateEntity) {
        if (Objects.nonNull(serviceTemplateEntity)
                && Objects.nonNull(serviceTemplateEntity.getOcl())) {
            ServiceTemplateDetailVo serviceTemplateDetailVo = new ServiceTemplateDetailVo();
            BeanUtils.copyProperties(serviceTemplateEntity, serviceTemplateDetailVo);
            serviceTemplateDetailVo.setServiceTemplateId(serviceTemplateEntity.getId());
            serviceTemplateDetailVo.setIcon(serviceTemplateEntity.getOcl().getIcon());
            serviceTemplateDetailVo.setDescription(serviceTemplateEntity.getOcl().getDescription());
            serviceTemplateDetailVo.setBilling(serviceTemplateEntity.getOcl().getBilling());
            serviceTemplateDetailVo.setFlavors(serviceTemplateEntity.getOcl().getFlavors());
            serviceTemplateDetailVo.setDeployment(serviceTemplateEntity.getOcl().getDeployment());
            serviceTemplateDetailVo.setRegions(
                    serviceTemplateEntity.getOcl().getCloudServiceProvider().getRegions());
            serviceTemplateDetailVo.add(
                    WebMvcLinkBuilder.linkTo(
                                    WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                                            .openApi(serviceTemplateEntity.getId()))
                            .withRel("openApi"));
            serviceTemplateDetailVo.setServiceHostingType(
                    serviceTemplateEntity.getOcl().getServiceHostingType());
            serviceTemplateDetailVo.setServiceProviderContactDetails(
                    serviceTemplateEntity.getOcl().getServiceProviderContactDetails());
            serviceTemplateDetailVo.setEula(serviceTemplateEntity.getOcl().getEula());
            serviceTemplateDetailVo.setServiceConfigurationManage(
                    serviceTemplateEntity.getOcl().getServiceConfigurationManage());
            serviceTemplateDetailVo.setServiceActions(
                    serviceTemplateEntity.getOcl().getServiceActions());
            serviceTemplateDetailVo.setServiceObjects(
                    serviceTemplateEntity.getOcl().getServiceObjects());
            return serviceTemplateDetailVo;
        }
        return null;
    }

    /**
     * Convert ServiceTemplateEntity to UserOrderableServiceVo.
     *
     * @param serviceTemplateEntity serviceTemplateEntity
     * @return userOrderableServiceVo
     */
    public static UserOrderableServiceVo convertToUserOrderableServiceVo(
            ServiceTemplateEntity serviceTemplateEntity) {
        if (Objects.nonNull(serviceTemplateEntity)
                && Objects.nonNull(serviceTemplateEntity.getOcl())) {
            UserOrderableServiceVo userOrderableServiceVo = new UserOrderableServiceVo();
            BeanUtils.copyProperties(serviceTemplateEntity, userOrderableServiceVo);
            userOrderableServiceVo.setServiceTemplateId(serviceTemplateEntity.getId());
            userOrderableServiceVo.setIcon(serviceTemplateEntity.getOcl().getIcon());
            userOrderableServiceVo.setDescription(serviceTemplateEntity.getOcl().getDescription());
            userOrderableServiceVo.setBilling(serviceTemplateEntity.getOcl().getBilling());
            setFlavorsWithoutPricing(
                    userOrderableServiceVo, serviceTemplateEntity.getOcl().getFlavors());
            List<InputVariable> inputVariables =
                    DeploymentVariableHelper.getInputVariables(
                            serviceTemplateEntity.getOcl().getDeployment());
            userOrderableServiceVo.setInputVariables(inputVariables);
            List<OutputVariable> outputVariables =
                    DeploymentVariableHelper.getOutputVariables(
                            serviceTemplateEntity.getOcl().getDeployment());
            userOrderableServiceVo.setOutputVariables(outputVariables);
            userOrderableServiceVo.setRegions(
                    serviceTemplateEntity.getOcl().getCloudServiceProvider().getRegions());
            userOrderableServiceVo.add(
                    WebMvcLinkBuilder.linkTo(
                                    WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                                            .openApi(serviceTemplateEntity.getId()))
                            .withRel("openApi"));
            userOrderableServiceVo.setServiceHostingType(
                    serviceTemplateEntity.getOcl().getServiceHostingType());
            userOrderableServiceVo.setServiceProviderContactDetails(
                    serviceTemplateEntity.getOcl().getServiceProviderContactDetails());
            userOrderableServiceVo.setServiceAvailabilityConfig(
                    serviceTemplateEntity.getOcl().getDeployment().getServiceAvailabilityConfig());
            userOrderableServiceVo.setEula(serviceTemplateEntity.getOcl().getEula());
            userOrderableServiceVo.setServiceActions(
                    serviceTemplateEntity.getOcl().getServiceActions());
            userOrderableServiceVo.setServiceObjects(
                    serviceTemplateEntity.getOcl().getServiceObjects());
            ServiceChangeManage serviceConfigurationManage =
                    serviceTemplateEntity.getOcl().getServiceConfigurationManage();
            if (Objects.nonNull(serviceConfigurationManage)) {
                userOrderableServiceVo.setConfigurationParameters(
                        serviceConfigurationManage.getConfigurationParameters());
            }
            return userOrderableServiceVo;
        }
        return null;
    }

    private static void setFlavorsWithoutPricing(
            UserOrderableServiceVo serviceVo, FlavorsWithPrice flavors) {
        List<ServiceFlavor> flavorBasics =
                flavors.getServiceFlavors().stream()
                        .map(
                                flavor -> {
                                    ServiceFlavor flavorBasic = new ServiceFlavor();
                                    BeanUtils.copyProperties(flavor, flavorBasic);
                                    return flavorBasic;
                                })
                        .toList();
        EndUserFlavors endUserFlavors = new EndUserFlavors();
        endUserFlavors.setServiceFlavors(flavorBasics);
        endUserFlavors.setDowngradeAllowed(flavors.getIsDowngradeAllowed());
        endUserFlavors.setModificationImpact(flavors.getModificationImpact());
        serviceVo.setFlavors(endUserFlavors);
    }
}
