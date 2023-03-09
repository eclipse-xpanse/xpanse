/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.hibernate.annotations.Type;

/**
 * DeployServiceEntity for persistence.
 */
@Table(name = "DEPLOY_SERVICE")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DeployServiceEntity extends CreateModifiedTime {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "VERSION", nullable = false)
    private String version;

    @Column(name = "CSP", nullable = false)
    @Enumerated(EnumType.STRING)
    private Csp csp;

    @Column(name = "CATEGORY")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "SERVICE_STATE")
    @Enumerated(EnumType.STRING)
    private ServiceState serviceState;

    /**
     * The flavor of the Service.
     */
    @Column(name = "FLAVOR")
    private String flavor;

    @Column(columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private CreateRequest createRequest;

    @Column(columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private DeployResult deployResult;

}
