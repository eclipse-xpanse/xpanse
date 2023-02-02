/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

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
    private List<String> subnet;
    private List<String> security;
    private List<String> storage;
    private boolean publicly;

}
