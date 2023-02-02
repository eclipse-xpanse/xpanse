/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import java.util.List;
import lombok.Data;

/**
 * Defines the network details on which the managed service is deployed.
 */
@Data
public class Network {

    private String id;
    private List<Vpc> vpc;
    private List<Subnet> subnet;
    private List<Security> security;

}
