/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicerecreate;

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
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;

/**
 * ServiceRecreateEntity for persistence.
 */
@Table(name = "SERVICE_RECREATE")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceRecreateEntity extends CreateModifiedTime {

    @Id
    @Column(name = "RECREATE_ID", nullable = false)
    private UUID recreateId;

    @Column(name = "SERVICE_ID", nullable = false)
    private UUID serviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "RECREATE_STATUS")
    private RecreateStatus recreateStatus;

    @Column(name = "USER_ID", nullable = false)
    private String userId;
}
