/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.utils;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.Optional;

/**
 * Optional is converted into String serialization class.
 */
public class OptionalToStringSerializer extends StdConverter<Optional<String>, String> {

    /**
     * Optional convert String.
     */
    @Override
    public String convert(Optional<String> value) {
        return value.orElse(null);
    }
}
