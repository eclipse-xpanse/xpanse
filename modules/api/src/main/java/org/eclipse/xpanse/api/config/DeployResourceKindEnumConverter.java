/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ReusableCloudResource enum.
 */
@Component
public class DeployResourceKindEnumConverter
        implements Converter<String, DeployResourceKind> {

    @Override
    public DeployResourceKind convert(@Nonnull String kind) {
        return DeployResourceKind.getByValue(kind);
    }
}
