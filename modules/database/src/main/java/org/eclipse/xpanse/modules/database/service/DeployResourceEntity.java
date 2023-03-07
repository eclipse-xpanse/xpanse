/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.DeployResourceKind;

/**
 * DeployResourceEntity for persistence.
 */
@Data
@Table(name = "deployResource")
@Entity
public class DeployResourceEntity {

    /**
     * The id of the deployed resource.
     */
    private String id;

    /**
     * The name of the deployed resource.
     */
    private String name;

    /**
     * The kind of the deployed resource.
     */
    private DeployResourceKind kind;

    /**
     * The deployService we belonged to.
     */
    @ManyToOne
    @JoinColumn(name = "deployService_id")
    DeployServiceEntity deployService;

    /**
     * The properties of the deployed resource.
     */
    @ElementCollection
    @CollectionTable(name = "deployResourceProperty",
            joinColumns = @JoinColumn(name = "deployResource_id", nullable = false))
    @MapKeyColumn(name = "p_key")
    @Column(name = "p_value")
    Map<String, String> property;
}
