/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicemigration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;

/**
 * ServiceMigrationEntity for persistence.
 */
@Table(name = "SERVICE_MIGRATION")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceMigrationEntity extends CreateModifiedTime {

    @Id
    @Column(name = "MIGRATION_ID", nullable = false)
    private UUID migrationId;

    @Column(name = "OLD_SERVICE_ID", nullable = false)
    private UUID oldServiceId;

    @Column(name = "NEW_SERVICE_ID", nullable = false)
    private UUID newServiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "MIGRATION_STATUS")
    private MigrationStatus migrationStatus;

    @Column(name = "USER_ID", nullable = false)
    private String userId;
}
