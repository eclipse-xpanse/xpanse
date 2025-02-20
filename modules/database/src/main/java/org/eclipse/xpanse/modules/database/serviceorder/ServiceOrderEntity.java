/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceorder;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/** ServiceOrderEntity for persistence. */
@Table(name = "SERVICE_ORDER")
@Entity
@Data
public class ServiceOrderEntity implements Serializable {

    @Serial private static final long serialVersionUID = 8759112775257851274L;

    @Id
    @Column(name = "ORDER_ID", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ServiceDeploymentEntity serviceDeploymentEntity;

    @Column(name = "PARENT_ORDER_ID")
    private UUID parentOrderId;

    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    @Column(name = "ORIGINAL_SERVICE_ID")
    private UUID originalServiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_TYPE", nullable = false)
    private ServiceOrderType taskType;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_STATUS", nullable = false)
    private TaskStatus taskStatus;

    @Column(name = "ERROR", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private ErrorResponse errorResponse;

    @CreatedDate
    @Column(name = "STARTED_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    private OffsetDateTime startedTime;

    @LastModifiedDate
    @Column(name = "COMPLETED_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    private OffsetDateTime completedTime;

    @Column(name = "REQUEST_BODY", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> requestBody;

    @Column(name = "RESULT_PROPERTIES", columnDefinition = "json", length = Integer.MAX_VALUE)
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> resultProperties;

    @Enumerated(EnumType.STRING)
    @Column(name = "HANDLER", nullable = false)
    private Handler handler;
}
