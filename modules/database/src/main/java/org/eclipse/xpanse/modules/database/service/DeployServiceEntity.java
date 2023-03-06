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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.common.OclConverter;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Csp;
import org.hibernate.annotations.Type;

/**
 * DeployServiceEntity for persistence.
 */
@Table(name = "deployService")
@Entity
@Data
public class DeployServiceEntity extends CreateModifiedTime {

    @Hidden
    @Id
    private UUID id;

    /**
     * The name of the Service.
     */
    @NotNull
    @NotBlank
    String name;

    /**
     * The version of the Service.
     */
    @NotNull
    @NotBlank
    String version;

    /**
     * The csp of the Service.
     */
    @NotNull
    Csp csp;

    /**
     * The flavor of the Service.
     */
    @NotNull
    @NotBlank
    String flavor;

    /**
     * The Ocl object of the XpanseDeployTask.
     */
    @Column(name = "ocl", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = OclConverter.class)
    private Ocl ocl;

    /**
     * The property of the Service.
     */
    @ElementCollection
    @CollectionTable(name = "deployServiceProperty",
            joinColumns = @JoinColumn(name = "deployService_id", nullable = false))
    @MapKeyColumn(name = "p_key")
    @Column(name = "p_value")
    Map<String, String> property = new HashMap<>();

    @OneToMany(mappedBy = "deployService")
    private List<DeployResourceEntity> deployResourceEntity;
}
