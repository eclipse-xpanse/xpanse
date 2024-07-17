/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.hibernate.annotations.Type;

/**
 * ServiceConfigurationUpdateRequest for persistence.
 */
@Table(name = "SERVICE_CONFIGURATION_UPDATE_REQUEST")
@Entity
@Data
public class ServiceConfigurationUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8759112725757851274L;

    @Id
    @Column(name = "ORDER_ID", nullable = false)
    private UUID orderId;

    @Column(name = "PROPERTIES", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> properties;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private ServiceConfigurationStatus status;

}
