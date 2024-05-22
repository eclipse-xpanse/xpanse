/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTaskStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to WorkFlowTaskStatus enum.
 */
@Component
public class WorkFlowTaskStatusEnumConverter implements Converter<String, WorkFlowTaskStatus> {

    @Override
    public WorkFlowTaskStatus convert(@Nonnull String status) {
        return WorkFlowTaskStatus.getByValue(status);
    }
}
