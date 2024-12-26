/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;

/** Validator for Deployment scripts source. */
@Slf4j
public class DeploymentValidator
        implements ConstraintValidator<DeploymentScriptsConstraint, Deployment> {

    @Override
    public boolean isValid(Deployment deployment, ConstraintValidatorContext context) {
        Map<String, String> scriptsMap = deployment.getScriptFiles();
        boolean hasValidScript = false;
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            for (Map.Entry<String, String> entry : scriptsMap.entrySet()) {
                if (StringUtils.isNotBlank(entry.getKey())
                        && StringUtils.isNotBlank(entry.getValue())) {
                    hasValidScript = true;
                    break;
                } else {
                    log.warn("Empty key or value found in scriptFiles: {}", entry);
                }
            }
        }
        return hasValidScript || Objects.nonNull(deployment.getScriptsRepo());
    }
}
