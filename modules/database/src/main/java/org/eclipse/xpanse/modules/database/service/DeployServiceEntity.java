/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Category;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Csp;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.DeployState;
import org.hibernate.annotations.Type;

/**
 * DeployServiceEntity for persistence.
 */
@Table(name = "deployService")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DeployServiceEntity extends CreateModifiedTime {

    @Hidden
    @Id
    private UUID id;

    /**
     * The category of the Service.
     */
    private Category category;

    /**
     * The name of the Service.
     */
    private String name;

    /**
     * The version of the Service.
     */
    private String version;

    /**
     * The csp of the Service.
     */
    private Csp csp;

    /**
     * The flavor of the Service.
     */
    private String flavor;

    /**
     * The Ocl object of the XpanseDeployTask.
     */
    @Column(name = "ocl", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Ocl ocl;

    /**
     * The property of the Service.
     */
    @ElementCollection
    @CollectionTable(name = "deployServiceProperty",
            joinColumns = @JoinColumn(name = "deployService_id", nullable = false))
    @MapKeyColumn(name = "p_key")
    @Column(name = "p_value")
    private Map<String, String> property = new HashMap<>();

    @OneToMany(mappedBy = "deployService")
    private List<DeployResourceEntity> deployResourceEntity;

}
