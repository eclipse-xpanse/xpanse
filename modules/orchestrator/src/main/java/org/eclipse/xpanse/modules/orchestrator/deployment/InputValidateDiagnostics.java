/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** This data class holds the diagnostics details returned by the Terraform validator. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputValidateDiagnostics {

    private String detail;
}
