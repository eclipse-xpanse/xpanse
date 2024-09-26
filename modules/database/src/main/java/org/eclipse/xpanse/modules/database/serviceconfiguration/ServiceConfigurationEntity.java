/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * ServiceConfigurationEntity for persistence.
 */
@Table(name = "SERVICE_CONFIGURATION")
@Entity
@Data
public class ServiceConfigurationEntity {

    @Id
    private UUID id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "service_id")
    @JsonIgnoreProperties({"deployResourceList", "serviceConfigurationEntity"})
    private DeployServiceEntity deployServiceEntity;

    @Column(columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> configuration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @Column(name = "CREATED_TIME")
    private OffsetDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @Column(name = "UPDATED_TIME")
    private OffsetDateTime updatedTime;
}
