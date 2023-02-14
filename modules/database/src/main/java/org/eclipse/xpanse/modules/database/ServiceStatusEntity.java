/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.ServiceState;
import org.eclipse.xpanse.modules.ocl.state.OclResources;
import org.hibernate.annotations.Type;

/**
 * Represents the SERVICE_STATUS table in the database.
 */
@Table(name = "SERVICE_STATUS")
@Entity
@Data
public class ServiceStatusEntity {

    @Column(name = "ID")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "SERVICE_NAME")
    private String serviceName;

    @Column(name = "SERVICE_STATUS")
    @Enumerated(EnumType.STRING)
    private ServiceState serviceState;

    @Column(name = "PLUGIN_NAME")
    private String pluginName;

    @Column(name = "OCL", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = OclToStringConverter.class)
    private Ocl ocl;

    @Column(name = "OCL_RESOURCES",  columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = OclResourcesConverter.class)
    private OclResources resources;

    @Column(name = "STATUS_MESSAGE")
    private String statusMessage;
}
