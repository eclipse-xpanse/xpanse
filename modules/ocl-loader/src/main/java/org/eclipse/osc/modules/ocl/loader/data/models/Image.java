/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;

import java.util.List;

@Data
public class Image {

    private List<Provisioner> provisioners;
    private List<BaseImage> base;
    private List<Artifact> artifacts;

}
