/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OclResources {

    String state = "inactive";
    List<OclResource> resources = new ArrayList<>();

}
