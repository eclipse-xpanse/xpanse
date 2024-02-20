/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.api.controllers.ServiceCatalogApi;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorBasic;
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
            serviceTemplateDetailVo.setIcon(serviceTemplateEntity.getOcl().getIcon());
            serviceTemplateDetailVo.setDescription(
                    serviceTemplateEntity.getOcl().getDescription());
            serviceTemplateDetailVo.setNamespace(serviceTemplateEntity.getOcl().getNamespace());
            serviceTemplateDetailVo.setBilling(serviceTemplateEntity.getOcl().getBilling());
            serviceTemplateDetailVo.setFlavors(serviceTemplateEntity.getOcl().getFlavors());
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
            userOrderableServiceVo.setIcon(serviceTemplateEntity.getOcl().getIcon());
            userOrderableServiceVo.setDescription(
                    serviceTemplateEntity.getOcl().getDescription());
            userOrderableServiceVo.setBilling(serviceTemplateEntity.getOcl().getBilling());
            List<FlavorBasic> flavorBasics = serviceTemplateEntity.getOcl().getFlavors()
                    .stream().map(flavor -> {
                        FlavorBasic flavorBasic = new FlavorBasic();
                        BeanUtils.copyProperties(flavor, flavorBasic);
                        return flavorBasic;
                    }).toList();
            userOrderableServiceVo.setFlavors(flavorBasics);
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
            return userOrderableServiceVo;
        }
        return null;
    }
}
