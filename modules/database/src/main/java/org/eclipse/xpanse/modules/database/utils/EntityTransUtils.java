/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */


package org.eclipse.xpanse.modules.database.utils;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.enums.ResourceKindInstance;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

/**
 * Transform DB entity object and model object.
 */
@Slf4j
public class EntityTransUtils {


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
                DeployResource deployResource = ResourceKindInstance.getInstance(entity.getKind());
                BeanUtils.copyProperties(entity, deployResource);
                fillChildFields(deployResource, entity.getProperties());
                resources.add(deployResource);
            }
        }
        return resources;
    }


    private static void fillChildFields(DeployResource deployResource,
            Map<String, String> properties) {
        if (Objects.isNull(deployResource) || properties.isEmpty()) {
            return;
        }
        Field[] fields = deployResource.getClass().getFields();
        try {
            for (Field field : fields) {
                String fieldName = field.getName();
                if (properties.containsKey(fieldName)) {
                    String value = properties.get(fieldName);
                    field.setAccessible(true);
                    field.set(deployResource, value);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException:", e);
        }

    }

}
