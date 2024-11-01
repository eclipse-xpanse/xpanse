/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to manage service data which contains sensitive data.
 */
@Slf4j
@Component
public class SensitiveDataHandler {

    @Resource
    private AesUtil aesUtil;

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Method to mask all sensitive data after deployment is completed.
     *
     * @param serviceDeploymentEntity entity to be updated.
     */
    public void maskSensitiveFields(ServiceDeploymentEntity serviceDeploymentEntity) {
        log.debug("masking sensitive input data after deployment");
        if (Objects.nonNull(serviceDeploymentEntity
                .getDeployRequest().getServiceRequestProperties())) {
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(
                            serviceDeploymentEntity.getServiceTemplateId());
            for (DeployVariable deployVariable
                    : serviceTemplateEntity.getOcl().getDeployment()
                    .getVariables()) {
                if (deployVariable.getSensitiveScope() != SensitiveScope.NONE
                        && (serviceDeploymentEntity.getDeployRequest().getServiceRequestProperties()
                        .containsKey(deployVariable.getName()))) {
                    serviceDeploymentEntity.getDeployRequest().getServiceRequestProperties()
                            .put(deployVariable.getName(), "********");

                }
            }
        }
    }

    /**
     * Method to encode all sensitive data in the request.
     *
     * @param serviceTemplate service template of the deployment request.
     * @param serviceRequestProperties request properties sent by customer.
     */
    public void encodeDeployVariable(ServiceTemplateEntity serviceTemplate,
                                      Map<String, Object> serviceRequestProperties) {
        if (Objects.isNull(serviceTemplate.getOcl().getDeployment())
                ||
                CollectionUtils.isEmpty(serviceTemplate.getOcl().getDeployment().getVariables())
                || Objects.isNull(serviceRequestProperties)) {
            return;
        }
        serviceTemplate.getOcl().getDeployment().getVariables().forEach(variable -> {
            if (Objects.nonNull(variable) && !SensitiveScope.NONE.toValue()
                    .equals(variable.getSensitiveScope().toValue())
                    && serviceRequestProperties.containsKey(variable.getName())) {
                serviceRequestProperties.put(variable.getName(),
                        aesUtil.encode(
                                serviceRequestProperties.get(variable.getName()).toString()));
            }
        });
    }
}
