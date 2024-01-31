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
 * Destroy Scenario. The destroy scenario is used to set into TerraformResult and sent back to the
 * client which sent the destroy request in what scenario.
 */
public enum DestroyScenario {
    DESTROY("destroy"),
    ROLLBACK("rollback"),
    PURGE("purge");
    private final String scenario;

    DestroyScenario(String scenario) {
        this.scenario = scenario;
    }

    /**
     * For DestroyScenario deserialize.
     */
    @JsonCreator
    public static DestroyScenario getByValue(String scenario) {
        for (DestroyScenario destroyScenario : values()) {
            if (StringUtils.equalsIgnoreCase(destroyScenario.scenario, scenario)) {
                return destroyScenario;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DestroyScenario value %s is not supported.", scenario));
    }

    /**
     * For DestroyScenario serialize.
     */
    @JsonValue
    public String toValue() {
        return this.scenario;
    }
}
