/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 * TfStateResource class.
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TfStateResource {

    public String type;

    public String name;

    public List<TfStateResourceInstance> instances;
}
