/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;

/**
 * DeployResourceEntity for persistence.
 */
@Data
@Table(name = "DEPLOY_RESOURCE")
@Entity
@EqualsAndHashCode(callSuper = true)
public class DeployResourceEntity extends CreateModifiedTime {

    /**
     * The id of the entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    private String resourceId;

    /**
     * The name of the deployed resource.
     */
    private String name;

    /**
     * The kind of the deployed resource.
     */
    @Enumerated(EnumType.STRING)
    private DeployResourceKind kind;

    /**
     * The deployService we belonged to.
     */
    @ManyToOne
    @JoinColumn(name = "deployService_id")
    @JsonIgnoreProperties({"deployResourceEntity"})
    private DeployServiceEntity deployService;

    /**
     * The properties of the deployed resource.
     */
    @ElementCollection
    @CollectionTable(name = "DEPLOY_RESOURCE_PROPERTY",
            joinColumns = @JoinColumn(name = "deployResource_id", nullable = false))
    @MapKeyColumn(name = "p_key")
    @Column(name = "p_value")
    private Map<String, String> properties;
}
