/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 * TfStateResource class.
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TfStateResource {

    /**
     * Tf Resource type.
     */
    private String type;

    /**
     * Tf Resource name.
     */
    private String name;

    /**
     * Tf Resource mode. data: data resource; managed: new create resource.
     */
    private String mode;

    /**
     * List of Tf Resource instance.
     */
    private List<TfStateResourceInstance> instances;
}
