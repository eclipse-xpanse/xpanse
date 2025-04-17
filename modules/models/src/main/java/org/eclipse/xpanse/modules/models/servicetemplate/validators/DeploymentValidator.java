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
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;

/** Validator for Deployment scripts source. */
@Slf4j
public class DeploymentValidator
        implements ConstraintValidator<DeploymentConfigurationConstraint, Deployment> {

    @Override
    public boolean isValid(Deployment deployment, ConstraintValidatorContext context) {
        boolean isRequestValid = true;
        if (deployment.getTerraformDeployment() != null) {
            Map<String, String> scriptsMap = deployment.getTerraformDeployment().getScriptFiles();
            if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
                for (Map.Entry<String, String> entry : scriptsMap.entrySet()) {
                    if (StringUtils.isNotBlank(entry.getKey())
                            && StringUtils.isNotBlank(entry.getValue())) {
                        break;
                    } else {
                        log.warn("Empty key or value found in scriptFiles: {}", entry);
                        context.buildConstraintViolationWithTemplate(
                                        String.format(
                                                "Empty key or value found in scriptFiles: %s",
                                                entry))
                                .addConstraintViolation();
                        isRequestValid = false;
                    }
                }
            }
        }
        if (deployment.getDeployerTool().getKind() == DeployerKind.HELM) {
            if (Objects.isNull(deployment.getHelmDeployment())) {
                log.warn("HelmDeployment object is mandatory if deployer kind is Helm");
                context.buildConstraintViolationWithTemplate(
                                "HelmDeployment object is mandatory if deployer kind is Helm")
                        .addConstraintViolation();
                isRequestValid = false;
            } else if (Objects.nonNull(deployment.getTerraformDeployment())) {
                log.warn("TerraformDeployment is not allowed when deployer kind is Helm");
                context.buildConstraintViolationWithTemplate(
                                "TerraformDeployment is not allowed when deployer kind is Helm")
                        .addConstraintViolation();
                isRequestValid = false;
            }
        }
        if (deployment.getDeployerTool().getKind() == DeployerKind.TERRAFORM
                || deployment.getDeployerTool().getKind() == DeployerKind.OPEN_TOFU) {
            if (Objects.isNull(deployment.getTerraformDeployment())) {
                log.warn(
                        "TerraformDeployment object is mandatory if deployer kind is {}",
                        deployment.getDeployerTool().getKind());
                context.buildConstraintViolationWithTemplate(
                                String.format(
                                        "TerraformDeployment object is mandatory if deployer kind"
                                                + " is %s",
                                        deployment.getDeployerTool().getKind()))
                        .addConstraintViolation();
                isRequestValid = false;
            } else if (Objects.nonNull(deployment.getHelmDeployment())) {
                log.warn(
                        "HelmDeployment is not allowed when deployer kind is {}",
                        deployment.getDeployerTool().getKind());
                context.buildConstraintViolationWithTemplate(
                                String.format(
                                        "HelmDeployment is not allowed when deployer kind is %s",
                                        deployment.getDeployerTool().getKind()))
                        .addConstraintViolation();
                isRequestValid = false;
            }
        }
        return isRequestValid;
    }
}
