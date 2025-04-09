/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicechange;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.database.common.CreatedModifiedTime;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.servicechange.AnsibleTaskResult;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

/** ServiceChangeRequestEntity for persistence. */
@EqualsAndHashCode(callSuper = true)
@Table(name = "SERVICE_CHANGE_REQUEST")
@Entity
@Data
public class ServiceChangeRequestEntity extends CreatedModifiedTime implements Serializable {

    @Serial private static final long serialVersionUID = 8759112725757851274L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ServiceOrderEntity serviceOrderEntity;

    @ManyToOne
    @JoinColumn(name = "SERVICE_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ServiceDeploymentEntity serviceDeploymentEntity;

    @Column(name = "RESOURCE_NAME", nullable = false)
    private String resourceName;

    @Column(name = "CHANGE_HANDLER", nullable = false)
    private String changeHandler;

    @Column(name = "RESULT_MESSAGE")
    private String resultMessage;

    @Column(name = "PROPERTIES", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> properties;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ServiceChangeStatus status;

    @Column(name = "TASK_RESULT", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private List<AnsibleTaskResult> tasks;

    @Column(name = "ORIGINAL_REQUEST_PROPERTIES", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> originalRequestProperties;
}
