/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.utils;

import static org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper.TF_STATE_FILE_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfStateResourceInstance;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.springframework.util.CollectionUtils;

/** Utils Define methods to transform TfResource into DeployResource. */
@Slf4j
public class TfResourceTransUtils {

    private TfResourceTransUtils() {
        // private constructor to block instantiation.
    }

    /**
     * Fill DeployResource by getting the value of key property from TfStateResourceInstance.
     *
     * @param instance TfStateResourceInstance
     * @param deployResource DeployResource
     * @param keyProperties important properties from the instanceAttributes that must be recorded.
     */
    public static void fillDeployResource(
            TfStateResourceInstance instance,
            DeployResource deployResource,
            Map<String, String> keyProperties) {
        Map<String, Object> instanceAttributes = instance.getAttributes();
        if (Objects.isNull(instanceAttributes) || instanceAttributes.isEmpty()) {
            return;
        }
        String resourceId = getValue(instanceAttributes, "id");
        deployResource.setResourceId(resourceId);
        String resourceName = getValue(instanceAttributes, "name");
        if (StringUtils.isNotBlank(resourceName)) {
            deployResource.setResourceName(resourceName);
        } else {
            deployResource.setResourceName(resourceId);
        }
        deployResource.setProperties(new HashMap<>());
        if (Objects.nonNull(keyProperties) && !keyProperties.isEmpty()) {
            keyProperties.forEach(
                    (k, v) ->
                            deployResource.getProperties().put(k, getValue(instanceAttributes, v)));
        }
    }

    private static String getValue(Map<String, Object> instanceAttributes, String key) {
        if (Objects.isNull(instanceAttributes)) {
            return null;
        }
        if (Objects.nonNull(instanceAttributes.getOrDefault(key, StringUtils.EMPTY))) {
            return instanceAttributes.getOrDefault(key, StringUtils.EMPTY).toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Method to extract stored service resource state from DB entity. In case of terraform, it is
     * the tfstate file contents which we store after the service is successfully deployed.
     *
     * @param serviceDeploymentEntity DeployServiceEntity of the deployed service.
     * @return returns the resource state stored in the database.
     */
    public static String getStoredStateContent(ServiceDeploymentEntity serviceDeploymentEntity) {
        if (Objects.isNull(serviceDeploymentEntity)
                || CollectionUtils.isEmpty(serviceDeploymentEntity.getDeploymentGeneratedFiles())
                || StringUtils.isEmpty(
                        serviceDeploymentEntity
                                .getDeploymentGeneratedFiles()
                                .get(TF_STATE_FILE_NAME))) {
            throw new ServiceNotDeployedException(
                    "Can't find valid state context in stored deployed service.");
        }
        return serviceDeploymentEntity.getDeploymentGeneratedFiles().get(TF_STATE_FILE_NAME);
    }
}
