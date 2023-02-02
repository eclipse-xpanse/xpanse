/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import java.util.List;
import lombok.Data;

/**
 * Defines the image details for the managed service.
 */
@Data
public class Image {

    private List<Provisioner> provisioners;
    private List<BaseImage> base;
    private List<Artifact> artifacts;

}
