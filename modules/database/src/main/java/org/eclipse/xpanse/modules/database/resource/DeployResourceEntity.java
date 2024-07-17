/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

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
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;

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

    /**
     * The type of the group which configuration the deployed resource.
     */
    @Column(name = "GROUP_TYPE")
    private String groupType;

    /**
     * The name of the group which configuration the deployed resource.
     */
    @Column(name = "GROUP_NAME")
    private String groupName;

    /**
     * The id of the deployed resource.
     */
    @Column(name = "RESOURCE_ID")
    private String resourceId;

    /**
     * The name of the deployed resource.
     */
    @Column(name = "RESOURCE_NAME")
    private String resourceName;

    /**
     * The kind of the deployed resource.
     */
    @Column(name = "RESOURCE_KIND")
    @Enumerated(EnumType.STRING)
    private DeployResourceKind resourceKind;

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
    @Column(name = "P_VALUE", length = Integer.MAX_VALUE)
    private Map<String, String> properties;
}
