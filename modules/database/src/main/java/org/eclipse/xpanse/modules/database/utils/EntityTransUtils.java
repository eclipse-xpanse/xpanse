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
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
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

}
