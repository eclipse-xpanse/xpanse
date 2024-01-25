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

/**
 * OpenTofuExecutorResult.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuExecutorResult extends SystemCmdResult {

    private String terraformState;
    private Map<String, String> importantFileContentMap = new HashMap<>();

}