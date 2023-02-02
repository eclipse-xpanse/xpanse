/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResources;
import org.springframework.core.env.Environment;

/**
 * Class to hold all runtime information of the builder.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BuilderContext extends HashMap<String, Map<String, String>> {

    private Environment environment;

    private OclResources oclResources = new OclResources();
}
