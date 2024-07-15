/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceorder;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * ServiceOrderEntity for persistence.
 */
@Table(name = "SERVICE_ORDER")
@Entity
@Data
public class ServiceOrderEntity {

    @Id
    @Column(name = "ORDER_ID", nullable = false)
    private UUID orderId;

    @Column(name = "SERVICE_ID", nullable = false)
    private UUID serviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_TYPE", nullable = false)
    private ServiceOrderType taskType;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

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

    @Column(name = "PREVIOUS_DEPLOYE_REQUEST", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private DeployRequest previousDeployRequest;

    @Column(name = "NEW_DEPLOYE_REQUEST", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private DeployRequest newDeployRequest;

    @Column(name = "PREVIOUS_DEPLOYED_RESOURCES", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private List<DeployResource> previousDeployedResources;

    @Column(name = "PREVIOUS_DEPLOYED_RESULT_PROPERTY", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, String> previousDeployedServiceProperties;

    @Column(name = "PREVIOUS_DEPLOYED_SERVICE_PROPERTY", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, String> previousDeployedResultProperties;

}
