/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.security.secrets.SecretsManager;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service data which contains sensitive data. */
@Slf4j
@Component
public class SensitiveDataHandler {

    private static final String REQUEST_PROPERTIES_FIELD = "serviceRequestProperties";
    private static final String SENSITIVE_VALUE = "******";

    @Resource private SecretsManager secretsManager;

    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Method to mask all sensitive data after deployment is completed.
     *
     * @param orderRequestBody orderRequestBody
     * @param definedVariables definedVariables
     */
    public Map<String, Object> getOrderRequestBodyWithSensitiveFields(
            Map<String, Object> orderRequestBody, List<DeployVariable> definedVariables) {
        log.debug("masking sensitive input data in request body after deployment");
        if (!CollectionUtils.isEmpty(orderRequestBody)) {
            Map<String, Object> serviceRequestProperties =
                    (Map<String, Object>)
                            orderRequestBody.getOrDefault(REQUEST_PROPERTIES_FIELD, null);
            if (!CollectionUtils.isEmpty(serviceRequestProperties)) {
                definedVariables.stream()
                        .filter(variable -> variable.getSensitiveScope() != SensitiveScope.NONE)
                        .forEach(
                                variable -> {
                                    if (serviceRequestProperties.containsKey(variable.getName())) {
                                        serviceRequestProperties.put(
                                                variable.getName(), SENSITIVE_VALUE);
                                    }
                                });
            }
            orderRequestBody.put(REQUEST_PROPERTIES_FIELD, serviceRequestProperties);
        }
        return orderRequestBody;
    }

    /**
     * Method to mask all sensitive data in deployed service view object.
     *
     * @param inputProperties inputProperties
     * @param definedVariables definedVariables
     */
    public Map<String, String> getServiceRequestPropertiesWithSensitiveFields(
            Map<String, String> inputProperties, List<DeployVariable> definedVariables) {
        if (!CollectionUtils.isEmpty(inputProperties)) {
            definedVariables.stream()
                    .filter(variable -> variable.getSensitiveScope() != SensitiveScope.NONE)
                    .forEach(
                            variable -> {
                                if (inputProperties.containsKey(variable.getName())) {
                                    inputProperties.put(variable.getName(), SENSITIVE_VALUE);
                                }
                            });
        }
        return inputProperties;
    }

    /**
     * Method to encode all sensitive data in the request.
     *
     * @param serviceTemplate service template of the deployment request.
     * @param serviceRequestProperties request properties sent by customer.
     */
    public void encodeDeployVariable(
            ServiceTemplateEntity serviceTemplate, Map<String, Object> serviceRequestProperties) {
        if (Objects.isNull(serviceTemplate.getOcl().getDeployment())
                || CollectionUtils.isEmpty(serviceTemplate.getOcl().getDeployment().getVariables())
                || Objects.isNull(serviceRequestProperties)) {
            return;
        }
        serviceTemplate
                .getOcl()
                .getDeployment()
                .getVariables()
                .forEach(
                        variable -> {
                            if (Objects.nonNull(variable)
                                    && variable.getSensitiveScope() != SensitiveScope.NONE
                                    && serviceRequestProperties.containsKey(variable.getName())) {
                                serviceRequestProperties.put(
                                        variable.getName(),
                                        secretsManager.encrypt(
                                                serviceRequestProperties
                                                        .get(variable.getName())
                                                        .toString()));
                            }
                        });
    }
}
