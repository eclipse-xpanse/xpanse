/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicemigration;

import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;

/**
 * The query model for search ServiceMigration.
 */
@Data
public class ServiceMigrationQueryModel {

    private UUID migrationId;

    private UUID oldServiceId;

    private UUID newServiceId;

    private MigrationStatus migrationStatus;

    private String userId;
}
