/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.resources.TfStateResourceInstance;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.springframework.util.CollectionUtils;

/**
 * Utils Define methods to transform TfResource into DeployResource.
 */
@Slf4j
public class TfResourceTransUtils {

    public static final String STATE_FILE_NAME = "terraform.tfstate";

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

    /**
     * Method to extract stored service resource state from DB entity. In case of opentofu, it is
     * the tfstate file contents which we store after the service is successfully deployed.
     *
     * @param deployServiceEntity DeployServiceEntity of the deployed service.
     * @return returns the resource state stored in the database.
     */

    public static String getStoredStateContent(DeployServiceEntity deployServiceEntity) {
        if (Objects.isNull(deployServiceEntity)
                || CollectionUtils.isEmpty(deployServiceEntity.getPrivateProperties())
                || StringUtils.isEmpty(
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME))) {
            throw new ServiceNotDeployedException(
                    "Can't find valid state context in stored deployed service.");
        }
        return deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
    }
}
