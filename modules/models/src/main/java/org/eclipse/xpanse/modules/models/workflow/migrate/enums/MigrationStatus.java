/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.workflow.migrate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Defines possible states of a service migration request.
 */
public enum MigrationStatus {

    MIGRATION_STARTED("MigrationStarted"),
    MIGRATION_COMPLETED("MigrationCompleted"),
    MIGRATION_FAILED("MigrationFailed"),

    DATA_EXPORT_STARTED("DataExportStarted"),
    DATA_EXPORT_FAILED("DataExportFailed"),
    DATA_EXPORT_COMPLETED("DataExportCompleted"),

    DEPLOY_STARTED("DeployStarted"),
    DEPLOY_FAILED("DeployFailed"),
    DEPLOY_COMPLETED("DeployCompleted"),

    DATA_IMPORT_STARTED("DataImportStarted"),
    DATA_IMPORT_FAILED("DataImportFailed"),
    DATA_IMPORT_COMPLETED("DataImportCompleted"),

    DESTROY_STARTED("DestroyStarted"),
    DESTROY_FAILED("DestroyFailed"),
    DESTROY_COMPLETED("DestroyCompleted");

    private final String state;

    MigrationStatus(String state) {
        this.state = state;
    }

    /**
     * For MigrationStatus deserialize.
     */
    @JsonCreator
    public static MigrationStatus getByValue(String state) {
        for (MigrationStatus migrationStatus : values()) {
            if (StringUtils.equalsIgnoreCase(migrationStatus.state, state)) {
                return migrationStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("MigrationStatus value %s is not supported.", state));
    }

    /**
     * For MigrationStatus serialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }

}
