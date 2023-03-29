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
import org.eclipse.xpanse.modules.models.service.Disk;
import org.eclipse.xpanse.modules.models.service.PublicIp;
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
    @SuppressWarnings("unchecked")
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
                String serviceType = tfStateResource.getType();
                Boolean create = true;
                if (StringUtils.equals(tfStateResource.getMode(), "data")) {
                    create = false;
                }
                if (StringUtils.equals(serviceType, "huaweicloud_compute_instance")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new Vm();
                        deployResource.setKind(DeployResourceKind.VM);
                        deployResource.setProperty(new HashMap<>());
                        deployResource.getProperty()
                                .put("create", String.valueOf(create));
                        deployResource.getProperty()
                                .put("service_type", serviceType);
                        fillResourceInfo(instance, deployResource);
                        deployResourceList.add(deployResource);
                    }
                }
                if (StringUtils.equals(serviceType, "huaweicloud_evs_volume")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new Disk();
                        deployResource.setProperty(new HashMap<>());
                        deployResource.getProperty()
                                .put("create", String.valueOf(create));
                        deployResource.getProperty()
                                .put("service_type", serviceType);
                        deployResource.setKind(DeployResourceKind.DISK);
                        fillResourceInfo(instance, deployResource);
                        deployResourceList.add(deployResource);
                    }
                }
                if (StringUtils.equals(serviceType, "huaweicloud_vpc_eip")) {
                    if (create) {
                        for (TfStateResourceInstance instance : tfStateResource.getInstances()) {

                            DeployResource deployResource = new PublicIp();
                            deployResource.setProperty(new HashMap<>());
                            deployResource.getProperty()
                                    .put("create", String.valueOf(create));
                            List<Map<String, Object>> bandwidth =
                                    (List<Map<String, Object>>) instance.getAttributes().get(
                                            "bandwidth");
                            String shareType = (String) bandwidth.get(0).get("share_type");
                            deployResource.getProperty()
                                    .put("service_type", serviceType);
                            List<Map<String, Object>> publicIp =
                                    (List<Map<String, Object>>) instance.getAttributes().get(
                                            "publicip");
                            String type = (String) publicIp.get(0).get("type");
                            deployResource.getProperty()
                                    .put("public_ip_type", type);
                            deployResource.getProperty()
                                    .put("bandwidth_share_type", shareType);
                            String size = bandwidth.get(0).get("size").toString();
                            deployResource.getProperty()
                                    .put("bandwidth_size", size);
                            String chargeMode = bandwidth.get(0).get("charge_mode").toString();
                            deployResource.getProperty()
                                    .put("charge_mode", chargeMode);
                            deployResource.setKind(DeployResourceKind.PUBLICIP);
                            fillResourceInfo(instance, deployResource);
                            deployResourceList.add(deployResource);
                        }
                    } else {
                        for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                            DeployResource deployResource = new PublicIp();
                            deployResource.setProperty(new HashMap<>());
                            deployResource.getProperty()
                                    .put("create", String.valueOf(create));
                            deployResource.getProperty()
                                    .put("service_type", serviceType);
                            deployResource.setKind(DeployResourceKind.PUBLICIP);
                            fillResourceInfo(instance, deployResource);
                            deployResourceList.add(deployResource);
                        }
                    }
                }
            }
        }

        deployResult.setResources(deployResourceList);
    }


    private void fillResourceInfo(TfStateResourceInstance instance,
            DeployResource deployResource) {
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
