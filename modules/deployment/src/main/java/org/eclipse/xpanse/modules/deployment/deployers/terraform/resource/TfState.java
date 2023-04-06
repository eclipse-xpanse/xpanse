/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * TfState class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TfState {

    private Map<String, TfOutput> outputs;

    private List<TfStateResource> resources;
}
