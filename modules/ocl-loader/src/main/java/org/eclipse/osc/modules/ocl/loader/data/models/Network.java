/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;

import java.util.List;

@Data
public class Network {

    private String id;
    private List<VPC> vpc;
    private List<Subnet> subnet;
    private List<Security> security;

}
