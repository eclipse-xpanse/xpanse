/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import jakarta.annotation.Nonnull;
import lombok.Data;

/**
 * Details of base Image on which the managed service is deployed.
 */
@Data
public class BaseImage {

    @Nonnull
    private String name;
    private String type;
    private BaseImageFilter filters;

}