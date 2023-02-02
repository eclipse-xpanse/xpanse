/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import java.util.List;
import lombok.Data;

/**
 * Defines all provisioning steps needed to fully deploy the managed service.
 */
@Data
public class Provisioner {

    private String name;
    private String type;
    private List<String> environments;
    private List<String> inline;

}
