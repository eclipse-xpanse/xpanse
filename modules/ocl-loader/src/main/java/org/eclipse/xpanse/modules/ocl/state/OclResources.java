/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.state;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.RuntimeState;

/**
 * List of OclResources.
 **/
@Data
public class OclResources {

    RuntimeState state = RuntimeState.INACTIVE;
    List<OclResource> resources = new ArrayList<>();

}
