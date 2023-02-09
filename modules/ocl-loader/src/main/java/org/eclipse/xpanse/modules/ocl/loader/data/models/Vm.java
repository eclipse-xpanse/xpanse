/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Defines the VM configuration required for the managed service to be deployed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Vm extends RuntimeBase {

    private String name;
    private String type;
    private String image;
    private List<String> subnets;
    private List<String> securityGroups;
    private List<String> storages;
    private boolean publicly;
    private UserData userData;

}
