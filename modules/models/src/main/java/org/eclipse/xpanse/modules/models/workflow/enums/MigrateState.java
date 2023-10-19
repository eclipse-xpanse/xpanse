/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.workflow.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Migration status enum.
 */
public enum MigrateState {
    MIGRATING("migrating"),
    MIGRATION_SUCCESS("migration_success"),
    MIGRATION_FAILED("migration_failed");

    private final String state;

    MigrateState(String state) {
        this.state = state;
    }

    /**
     * For MigrateState serialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}
