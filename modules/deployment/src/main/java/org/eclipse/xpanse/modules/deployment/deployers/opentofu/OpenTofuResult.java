/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DestroyScenario;

/**
 * OpenTofuExecutorResult.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuResult extends SystemCmdResult {

    private DestroyScenario destroyScenario;
    private String terraformState;
    private Map<String, String> importantFileContentMap = new HashMap<>();

}