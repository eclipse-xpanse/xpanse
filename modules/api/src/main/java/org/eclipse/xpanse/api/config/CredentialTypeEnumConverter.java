/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.plugin.credential.enums.CredentialType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to CredentialType enum.
 */
@Component
public class CredentialTypeEnumConverter implements Converter<String, CredentialType> {

    @Override
    public CredentialType convert(String type) {
        return CredentialType.getByValue(type);
    }
}
