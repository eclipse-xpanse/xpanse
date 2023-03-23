/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfOutput;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfState;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResourceInstance;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.eclipse.xpanse.modules.models.service.Vm;
import org.eclipse.xpanse.orchestrator.plugin.huaweicloud.models.HuaweiResourceProperty;
import org.springframework.stereotype.Component;

/**
 * Terraform resource handler for Huawei.
 */
@Component
@Slf4j
public class HuaweiTerraformResourceHandler implements DeployResourceHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handler of HuaweiCloud for the DeployResult.
     *
     * @param deployResult the result of the deployment.
     */
    @Override
    public void handler(DeployResult deployResult) {
        List<DeployResource> deployResourceList = new ArrayList<>();
        TfState tfState;
        try {
            var stateFile = deployResult.getProperty().get("stateFile");
            tfState = objectMapper.readValue(stateFile, TfState.class);
            deployResult.getProperty().remove("stateFile");
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }
        if (Objects.nonNull(tfState)) {
            if (!tfState.getOutputs().isEmpty()) {
                for (String outputKey : tfState.getOutputs().keySet()) {
                    TfOutput tfOutput = tfState.getOutputs().get(outputKey);
                    deployResult.getProperty().put(outputKey, tfOutput.getValue());
                }
            }
            for (TfStateResource tfStateResource : tfState.getResources()) {
                if (tfStateResource.getType().equals("huaweicloud_compute_instance")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new Vm();
                        deployResource.setKind(DeployResourceKind.Vm);
                        fillResourceInfo(instance, deployResource);

                        deployResourceList.add(deployResource);
                    }
                }
                if (tfStateResource.getType().equals("huaweicloud_compute_eip_associate")) {

                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new DeployResource();
                        deployResource.setProperty(new HashMap<>());
                        deployResource.getProperty()
                                .put("ip", (String) instance.getAttributes().get("public_ip"));
                        deployResource.setKind(DeployResourceKind.PublicIp);
                        deployResourceList.add(deployResource);
                    }
                }
                if (tfStateResource.getType().equals("huaweicloud_vpc")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new DeployResource();
                        deployResource.setKind(DeployResourceKind.Vpc);
                        deployResource.setResourceId((String) instance.getAttributes().get("id"));
                        deployResource.setName((String) instance.getAttributes().get("name"));
                        deployResourceList.add(deployResource);
                    }
                }
            }
        }

        deployResult.setResources(deployResourceList);
    }


    private void fillResourceInfo(TfStateResourceInstance instance,
            DeployResource deployResource) {
        deployResource.setProperty(new HashMap<>());
        Map<String, Object> instanceAttributes = instance.getAttributes();
        if (Objects.isNull(instanceAttributes) || instanceAttributes.isEmpty()) {
            return;
        }
        deployResource.setResourceId(getValue(instanceAttributes, "id"));
        deployResource.setName(getValue(instanceAttributes, "name"));
        Map<String, String> keyMap = HuaweiResourceProperty.getProperties(deployResource.getKind());

        Field[] fields = deployResource.getClass().getFields();
        Set<String> fieldSet =
                Arrays.stream(fields).map(Field::getName).collect(Collectors.toSet());
        try {
            for (Field field : fields) {
                String fieldName = field.getName();
                if (keyMap.containsKey(fieldName)) {
                    String key = keyMap.get(fieldName);
                    String value = getValue(instanceAttributes, key);
                    field.setAccessible(true);
                    field.set(deployResource, value);
                    deployResource.getProperty().put(fieldName, value);
                }
            }
            for (String key : keyMap.keySet()) {
                if (!fieldSet.contains(key)) {
                    String value = instanceAttributes.getOrDefault(key, StringUtils.EMPTY)
                            .toString();
                    deployResource.getProperty().put(key, value);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException:", e);
        }
    }

    private String getValue(Map<String, Object> instanceAttributes, String key) {
        if (Objects.isNull(instanceAttributes)) {
            return null;
        }
        return instanceAttributes.getOrDefault(key, StringUtils.EMPTY).toString();
    }
}
