/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResourceInstance;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;

/**
 * Utils Define methods to transform TfResource into DeployResource.
 */
@Slf4j
public class TfResourceTransUtils {

    private TfResourceTransUtils() {
        // private constructor to block instantiation.
    }

    /**
     * Fill DeployResource by getting the value of key property from TfStateResourceInstance.
     *
     * @param instance       TfStateResourceInstance
     * @param deployResource DeployResource
     * @param keyProperties  important properties from the instanceAttributes that must be recorded.
     */
    public static void fillDeployResource(TfStateResourceInstance instance,
                                          DeployResource deployResource,
                                          Map<String, String> keyProperties) {
        Map<String, Object> instanceAttributes = instance.getAttributes();
        if (Objects.isNull(instanceAttributes) || instanceAttributes.isEmpty()) {
            return;
        }
        String id = getValue(instanceAttributes, "id");
        deployResource.setResourceId(id);
        String name = getValue(instanceAttributes, "name");
        if (StringUtils.isBlank(name)) {
            deployResource.setName(deployResource.getKind().toValue() + "-" + id);
        } else {
            deployResource.setName(name);
        }
        deployResource.setProperties(new HashMap<>());
        if (Objects.nonNull(keyProperties) && !keyProperties.isEmpty()) {
            keyProperties.forEach((k, v) ->
                    deployResource.getProperties().put(k, getValue(instanceAttributes, v)));
        }

    }

    private static String getValue(Map<String, Object> instanceAttributes, String key) {
        if (Objects.isNull(instanceAttributes)) {
            return null;
        }
        return instanceAttributes.getOrDefault(key, StringUtils.EMPTY).toString();
    }

}
