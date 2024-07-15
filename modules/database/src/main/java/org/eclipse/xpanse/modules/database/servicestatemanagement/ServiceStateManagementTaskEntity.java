/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicestatemanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * ServiceStateManagementTaskEntity for persistence.
 */
@Table(name = "SERVICE_STATE_MANAGEMENT_TASK")
@Entity
@Data
public class ServiceStateManagementTaskEntity {

    @Id
    @Column(name = "TASK_ID", nullable = false)
    private UUID taskId;

    @Column(name = "SERVICE_ID", nullable = false)
    private UUID serviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_TYPE", nullable = false)
    private ServiceStateManagementTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_STATUS", nullable = false)
    private TaskStatus taskStatus;

    @Column(name = "ERROR_MSG", length = Integer.MAX_VALUE)
    private String errorMsg;

    @CreatedDate
    @Column(name = "STARTED_TIME")
    private OffsetDateTime startedTime;

    @LastModifiedDate
    @Column(name = "COMPLETED_TIME")
    private OffsetDateTime completedTime;
}