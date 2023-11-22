/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.modules.database.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

/**
 * Transform DB entity object and model object.
 */
@Slf4j
public class EntityTransUtils {

    private EntityTransUtils() {
        // block constructor.
    }


    /**
     * Transform ist of deployResourceEntity to list of DeployResource.
     *
     * @param entities list of deployResourceEntity
     * @return list of DeployResource
     */
    public static List<DeployResource> transResourceEntity(List<DeployResourceEntity> entities) {
        List<DeployResource> resources = new ArrayList<>();
        if (!CollectionUtils.isEmpty(entities)) {
            for (DeployResourceEntity entity : entities) {
                DeployResource deployResource = new DeployResource();
                BeanUtils.copyProperties(entity, deployResource);
                resources.add(deployResource);
            }
        }
        return resources;
    }

    /**
     * DeployServiceEntity converted to DeployedServiceDetails.
     *
     * @param deployServiceEntity DeployServiceEntity.
     * @return serviceDetailVo
     */
    public static DeployedServiceDetails transDeployServiceEntityToServiceDetailVo(
            DeployServiceEntity deployServiceEntity) {
        DeployedServiceDetails deployedServiceDetails = new DeployedServiceDetails();
        deployedServiceDetails.setServiceHostingType(
                deployServiceEntity.getDeployRequest().getServiceHostingType());
        BeanUtils.copyProperties(deployServiceEntity, deployedServiceDetails);
        if (!CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
            List<DeployResource> deployResources = transResourceEntity(
                    deployServiceEntity.getDeployResourceList());
            deployedServiceDetails.setDeployResources(deployResources);
        }
        if (!CollectionUtils.isEmpty(deployServiceEntity.getProperties())) {
            deployedServiceDetails.setDeployedServiceProperties(
                    deployServiceEntity.getProperties());
        }
        return deployedServiceDetails;
    }

    /**
     * DeployServiceEntity converted to VendorHostedDeployedServiceDetails.
     *
     * @param deployServiceEntity DeployServiceEntity.
     * @return serviceDetailVo
     */
    public static VendorHostedDeployedServiceDetails
            transServiceEntityToVendorHostedServiceDetailsVo(
            DeployServiceEntity deployServiceEntity) {
        VendorHostedDeployedServiceDetails vendorHostedDeployedServiceDetails =
                new VendorHostedDeployedServiceDetails();
        vendorHostedDeployedServiceDetails.setServiceHostingType(
                deployServiceEntity.getDeployRequest().getServiceHostingType());
        BeanUtils.copyProperties(deployServiceEntity, vendorHostedDeployedServiceDetails);
        if (!CollectionUtils.isEmpty(deployServiceEntity.getProperties())) {
            vendorHostedDeployedServiceDetails.setDeployedServiceProperties(
                    deployServiceEntity.getProperties());
        }
        return vendorHostedDeployedServiceDetails;
    }

}
