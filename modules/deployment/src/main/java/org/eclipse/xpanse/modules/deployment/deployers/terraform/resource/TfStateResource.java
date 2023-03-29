/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 * TfStateResource class.
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TfStateResource {

    public String mode;

    public String type;

    public String name;

    public List<TfStateResourceInstance> instances;

}
