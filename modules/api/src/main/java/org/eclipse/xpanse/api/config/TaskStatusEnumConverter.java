/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to TaskStatus enum. */
@Component
public class TaskStatusEnumConverter implements Converter<String, TaskStatus> {

    @Override
    public TaskStatus convert(@Nonnull String status) {
        return TaskStatus.getByValue(status);
    }
}
