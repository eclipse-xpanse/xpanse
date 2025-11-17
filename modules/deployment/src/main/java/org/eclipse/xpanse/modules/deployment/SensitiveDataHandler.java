/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.OutputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.security.secrets.SecretsManager;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service data which contains sensitive data. */
@Slf4j
@Component
public class SensitiveDataHandler {

    /** The value of sensitive data will be masked as "******". */
    public static final String SENSITIVE_VALUE = "******";

    private static final String REQUEST_PROPERTIES_FIELD = "serviceRequestProperties";

    @Resource private SecretsManager secretsManager;

    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;

    /**
     * Method to mask all sensitive data after deployment is completed.
     *
     * @param orderRequestBody orderRequestBody
     * @param definedVariables definedVariables
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOrderRequestBodyWithSensitiveFields(
            Map<String, Object> orderRequestBody, List<InputVariable> definedVariables) {
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
     * Method to encode all sensitive data in the request.
     *
     * @param inputVariables defined input variables in the service template.
     * @param serviceRequestProperties request properties sent by customer.
     */
    public void encodeInputVariables(
            List<InputVariable> inputVariables, Map<String, Object> serviceRequestProperties) {
        if (CollectionUtils.isEmpty(inputVariables)
                || CollectionUtils.isEmpty(serviceRequestProperties)) {
            return;
        }
        inputVariables.stream()
                .filter(variable -> variable.getSensitiveScope() != SensitiveScope.NONE)
                .forEach(
                        variable -> {
                            if (serviceRequestProperties.containsKey(variable.getName())) {
                                serviceRequestProperties.put(
                                        variable.getName(),
                                        secretsManager.encrypt(
                                                serviceRequestProperties
                                                        .get(variable.getName())
                                                        .toString()));
                            }
                        });
    }

    /**
     * Method to handle sensitive data in the input properties.
     *
     * @param inputVariables defined input variables in the service template
     * @param inputProperties input properties send by customer.
     */
    public void handleSensitiveDataInInputProperties(
            List<InputVariable> inputVariables, Map<String, String> inputProperties) {
        if (CollectionUtils.isEmpty(inputVariables) || CollectionUtils.isEmpty(inputProperties)) {
            return;
        }
        inputVariables.stream()
                .filter(variable -> variable.getSensitiveScope() != SensitiveScope.NONE)
                .forEach(
                        variable -> {
                            if (inputProperties.containsKey(variable.getName())) {
                                inputProperties.put(variable.getName(), SENSITIVE_VALUE);
                            }
                        });
    }

    /**
     * Method to encode all sensitive data in the output properties.
     *
     * @param outputVariables defined output variables in the service template.
     * @param outputProperties output properties of the service deployment.
     */
    public void encodeOutputVariables(
            List<OutputVariable> outputVariables, Map<String, String> outputProperties) {
        if (CollectionUtils.isEmpty(outputVariables) || CollectionUtils.isEmpty(outputProperties)) {
            return;
        }
        outputVariables.stream()
                .filter(variable -> variable.getSensitiveScope() != SensitiveScope.NONE)
                .forEach(
                        variable -> {
                            if (outputProperties.containsKey(variable.getName())) {
                                String encodedValue =
                                        secretsManager.encrypt(
                                                outputProperties.get(variable.getName()));
                                outputProperties.put(variable.getName(), encodedValue);
                            }
                        });
    }

    /**
     * Method to handle all sensitive data in the output properties.
     *
     * @param outputVariables defined output variables in the service template.
     * @param outputProperties output properties of the service deployment.
     */
    public void handleSensitiveDataInOutputVariables(
            UUID serviceId,
            List<OutputVariable> outputVariables,
            Map<String, String> outputProperties) {
        if (CollectionUtils.isEmpty(outputVariables) || CollectionUtils.isEmpty(outputProperties)) {
            return;
        }
        List<String> variablesWithSensitiveTypeOnce = new ArrayList<>();
        outputVariables.stream()
                .filter(variable -> variable.getSensitiveScope() == SensitiveScope.ALWAYS)
                .forEach(
                        variable -> {
                            if (outputProperties.containsKey(variable.getName())) {
                                String decodedValue =
                                        String.valueOf(
                                                secretsManager.decodeBackToOriginalType(
                                                        variable.getDataType(),
                                                        outputProperties.get(variable.getName())));
                                outputProperties.put(variable.getName(), decodedValue);
                                variablesWithSensitiveTypeOnce.add(variable.getName());
                            }
                        });

        outputVariables.stream()
                .filter(variable -> variable.getSensitiveScope() == SensitiveScope.ONCE)
                .forEach(
                        variable -> {
                            if (outputProperties.containsKey(variable.getName())) {
                                String decodedValue =
                                        getValueForSensitiveOnceField(
                                                outputProperties.get(variable.getName()));
                                outputProperties.put(variable.getName(), decodedValue);
                            }
                        });
        if (!variablesWithSensitiveTypeOnce.isEmpty()) {
            removeOnceViewedVariables(serviceId, variablesWithSensitiveTypeOnce);
        }
    }

    private String getValueForSensitiveOnceField(String encodedValue) {
        try {
            return secretsManager.decrypt(encodedValue);
        } catch (SensitiveFieldEncryptionOrDecryptionFailedException e) {
            log.info("decryption fails. This means, the value is already decrypted.");
            return encodedValue;
        }
    }

    private void removeOnceViewedVariables(UUID serviceId, List<String> onceViewedVariables) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        if (Objects.nonNull(serviceDeploymentEntity)
                && !CollectionUtils.isEmpty(serviceDeploymentEntity.getOutputProperties())) {
            onceViewedVariables.forEach(
                    variableName ->
                            serviceDeploymentEntity.getOutputProperties().remove(variableName));
            serviceDeploymentStorage.storeAndFlush(serviceDeploymentEntity);
        }
    }
}
