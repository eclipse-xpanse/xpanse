/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 * Defines the Terraform validation result.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TfValidationResult {

    private boolean valid;
    private List<TfValidateDiagnostics> diagnostics;
}
