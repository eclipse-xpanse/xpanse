/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to Category enum. */
@Component
public class CategoryEnumConverter implements Converter<String, Category> {

    @Override
    public Category convert(String category) {
        return Category.getByValue(category);
    }
}
