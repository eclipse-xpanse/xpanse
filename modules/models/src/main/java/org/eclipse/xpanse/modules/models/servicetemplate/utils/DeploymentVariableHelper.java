/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.OutputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;

/** Utility class for deployment. */
public class DeploymentVariableHelper {

    private DeploymentVariableHelper() {}

    /**
     * Get input variables from deployment.
     *
     * @param deployment deployment
     * @return input variables
     */
    public static List<InputVariable> getInputVariables(Deployment deployment) {
        if (deployment.getDeployerTool().getKind() == DeployerKind.TERRAFORM
                || deployment.getDeployerTool().getKind() == DeployerKind.OPEN_TOFU) {
            return deployment.getTerraformDeployment().getInputVariables();
        } else if (deployment.getDeployerTool().getKind() == DeployerKind.HELM) {
            return deployment.getHelmDeployment().getInputVariables();
        }
        return Collections.emptyList();
    }

    /**
     * Get output variables from deployment.
     *
     * @param deployment deployment
     * @return input variables
     */
    public static List<OutputVariable> getOutputVariables(Deployment deployment) {
        if (deployment.getDeployerTool().getKind() == DeployerKind.TERRAFORM
                || deployment.getDeployerTool().getKind() == DeployerKind.OPEN_TOFU) {
            return deployment.getTerraformDeployment().getOutputVariables();
        } else if (deployment.getDeployerTool().getKind() == DeployerKind.HELM) {
            return new ArrayList<>(deployment.getHelmDeployment().getHelmOutputVariables());
        }
        return Collections.emptyList();
    }
}
