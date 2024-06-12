/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.controllers.ServiceCatalogApi;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

/**
 * ServiceTemplateEntityConverter.
 */
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
            serviceTemplateDetailVo.setDescription(
                    serviceTemplateEntity.getOcl().getDescription());
            if (StringUtils.isNotEmpty(serviceTemplateEntity.getNamespace())) {
                serviceTemplateDetailVo.setNamespace(serviceTemplateEntity.getNamespace());
            } else {
                serviceTemplateDetailVo.setNamespace(serviceTemplateEntity.getOcl().getNamespace());
            }
            serviceTemplateDetailVo.setBilling(serviceTemplateEntity.getOcl().getBilling());
            serviceTemplateDetailVo.setFlavors(
                    serviceTemplateEntity.getOcl().getFlavors());
            serviceTemplateDetailVo.setDeployment(serviceTemplateEntity.getOcl().getDeployment());
            serviceTemplateDetailVo.setVariables(
                    serviceTemplateEntity.getOcl().getDeployment().getVariables());
            serviceTemplateDetailVo.setRegions(
                    serviceTemplateEntity.getOcl().getCloudServiceProvider().getRegions());
            serviceTemplateDetailVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                            .openApi(serviceTemplateEntity.getId().toString())).withRel("openApi"));
            serviceTemplateDetailVo.setServiceHostingType(
                    serviceTemplateEntity.getOcl().getServiceHostingType());
            serviceTemplateDetailVo.setServiceProviderContactDetails(
                    serviceTemplateEntity.getOcl().getServiceProviderContactDetails());
            serviceTemplateDetailVo.setEula(serviceTemplateEntity.getOcl().getEula());
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
            userOrderableServiceVo.setDescription(
                    serviceTemplateEntity.getOcl().getDescription());
            userOrderableServiceVo.setBilling(serviceTemplateEntity.getOcl().getBilling());

            setFlavorsWithoutPricing(userOrderableServiceVo,
                    serviceTemplateEntity.getOcl().getFlavors());

            userOrderableServiceVo.setVariables(
                    serviceTemplateEntity.getOcl().getDeployment().getVariables());
            userOrderableServiceVo.setRegions(
                    serviceTemplateEntity.getOcl().getCloudServiceProvider().getRegions());
            userOrderableServiceVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                            .openApi(serviceTemplateEntity.getId().toString())).withRel("openApi"));
            userOrderableServiceVo.setServiceHostingType(
                    serviceTemplateEntity.getOcl().getServiceHostingType());
            userOrderableServiceVo.setServiceProviderContactDetails(
                    serviceTemplateEntity.getOcl().getServiceProviderContactDetails());
            userOrderableServiceVo.setServiceAvailabilityConfigs(
                    serviceTemplateEntity.getOcl().getDeployment().getServiceAvailabilityConfigs());
            userOrderableServiceVo.setEula(serviceTemplateEntity.getOcl().getEula());
            return userOrderableServiceVo;
        }
        return null;
    }

    private static void setFlavorsWithoutPricing(UserOrderableServiceVo serviceVo,
                                                 FlavorsWithPrice flavors) {
        List<ServiceFlavor> flavorBasics = flavors.getServiceFlavors().stream().map(flavor -> {
            ServiceFlavor flavorBasic = new ServiceFlavor();
            BeanUtils.copyProperties(flavor, flavorBasic);
            return flavorBasic;
        }).toList();
        serviceVo.setFlavors(flavorBasics);
    }

}
