/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.terraform.deployment.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 * TfState class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TfState {

    public List<TfStateResource> resources;
}
