/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to Csp enum. */
@Component
public class CspEnumConverter implements Converter<String, Csp> {

    @Override
    public Csp convert(String csp) {
        return Csp.getByValue(csp);
    }
}
