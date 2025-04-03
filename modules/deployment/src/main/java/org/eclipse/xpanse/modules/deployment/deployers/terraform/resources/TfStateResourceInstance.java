/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;

/** TfStateResourceInstance class. */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TfStateResourceInstance {

    private String type;

    private Map<String, Object> attributes;
}
