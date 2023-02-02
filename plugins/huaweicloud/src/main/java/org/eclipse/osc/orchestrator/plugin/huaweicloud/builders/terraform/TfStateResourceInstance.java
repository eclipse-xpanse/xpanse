/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;

/**
 * TfStateResourceInstance class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TfStateResourceInstance {

    public Map<String, Object> attributes;
}
