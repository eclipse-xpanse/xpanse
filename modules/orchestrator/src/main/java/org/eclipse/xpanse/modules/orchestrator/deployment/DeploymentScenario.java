/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * The destroy scenario is used to set into TerraformResult and sent back to the
 * client which sent the destroy request in what scenario.
 */
public enum DeploymentScenario {
    DEPLOY("deploy"),
    MODIFY("modify"),
    DESTROY("destroy"),
    ROLLBACK("rollback"),
    PURGE("purge");
    private final String scenario;

    DeploymentScenario(String scenario) {
        this.scenario = scenario;
    }

    /**
     * For DeploymentScenario deserialize.
     */
    @JsonCreator
    public static DeploymentScenario getByValue(String scenario) {
        for (DeploymentScenario deploymentScenario : values()) {
            if (StringUtils.equalsIgnoreCase(deploymentScenario.scenario, scenario)) {
                return deploymentScenario;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeploymentScenario value %s is not supported.", scenario));
    }

    /**
     * For DestroyScenario serialize.
     */
    @JsonValue
    public String toValue() {
        return this.scenario;
    }
}
