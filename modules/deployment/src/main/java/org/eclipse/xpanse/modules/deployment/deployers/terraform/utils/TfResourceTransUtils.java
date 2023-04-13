/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResourceInstance;
import org.eclipse.xpanse.modules.models.service.DeployResource;

/**
 * Utils Define methods to transform TfResource into DeployResource.
 */
@Slf4j
public class TfResourceTransUtils {

    /**
     * Fill DeployResource by getting value of key property from TfStateResourceInstance.
     *
     * @param instance       TfStateResourceInstance
     * @param deployResource DeployResource
     * @param keyProperty    keyProperty
     */
    public static void fillDeployResource(TfStateResourceInstance instance,
            DeployResource deployResource, Map<String, String> keyProperty) {
        Map<String, Object> instanceAttributes = instance.getAttributes();
        if (Objects.isNull(instanceAttributes) || instanceAttributes.isEmpty()) {
            return;
        }
        deployResource.setResourceId(getValue(instanceAttributes, "id"));
        deployResource.setName(getValue(instanceAttributes, "name"));
        if (Objects.isNull(keyProperty) || keyProperty.isEmpty()) {
            return;
        }
        deployResource.setProperties(new HashMap<>());
        Field[] fields = deployResource.getClass().getFields();
        Set<String> fieldSet =
                Arrays.stream(fields).map(Field::getName).collect(Collectors.toSet());
        try {
            for (Field field : fields) {
                String fieldName = field.getName();
                if (keyProperty.containsKey(fieldName)) {
                    String key = keyProperty.get(fieldName);
                    String value = getValue(instanceAttributes, key);
                    field.setAccessible(true);
                    field.set(deployResource, value);
                    deployResource.getProperties().put(fieldName, value);
                }
            }
            for (String key : keyProperty.keySet()) {
                if (!fieldSet.contains(key)) {
                    String value = instanceAttributes.getOrDefault(key, StringUtils.EMPTY)
                            .toString();
                    deployResource.getProperties().put(key, value);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException:", e);
        }
    }

    private static String getValue(Map<String, Object> instanceAttributes, String key) {
        if (Objects.isNull(instanceAttributes)) {
            return null;
        }
        return instanceAttributes.getOrDefault(key, StringUtils.EMPTY).toString();
    }

}
