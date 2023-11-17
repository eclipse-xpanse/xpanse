/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
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
    private UUID id;

    /**
     * The id of user who deployed the service.
     */
    private String userId;

    /**
     * The category of the Service.
     */
    @Enumerated(EnumType.STRING)
    private Category category;

    /**
     * The name of the Service.
     */
    private String name;

    /**
     * The customer provided name for the service deployed.
     */
    private String customerServiceName;

    /**
     * The version of the Service.
     */
    private String version;

    /**
     * Namespace of the user who registered service template.
     */
    private String namespace;

    /**
     * The csp of the Service.
     */
    @Enumerated(EnumType.STRING)
    private Csp csp;

    /**
     * The flavor of the Service.
     */
    private String flavor;

    /**
     * The state of the Service.
     */
    @Enumerated(EnumType.STRING)
    private ServiceDeploymentState serviceDeploymentState;

    /**
     * The Ocl object of the XpanseDeployTask.
     */
    @Column(columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private DeployRequest deployRequest;

    @OneToMany(mappedBy = "deployService", orphanRemoval = true)
    @Cascade({CascadeType.ALL})
    @ToString.Exclude
    private List<DeployResourceEntity> deployResourceList;

    /**
     * The properties of the deployed service.
     */
    @ElementCollection
    @CollectionTable(name = "DEPLOY_SERVICE_PROPERTY",
            joinColumns = @JoinColumn(name = "DEPLOY_SERVICE_ID", nullable = false))
    @MapKeyColumn(name = "P_KEY")
    @Column(name = "P_VALUE", length = Integer.MAX_VALUE)
    private Map<String, String> properties;

    /**
     * The properties of the deployed service.
     */
    @ElementCollection
    @CollectionTable(name = "DEPLOY_RESULT_PROPERTY",
            joinColumns = @JoinColumn(name = "DEPLOY_SERVICE_ID", nullable = false))
    @MapKeyColumn(name = "P_KEY")
    @Column(name = "P_VALUE", length = Integer.MAX_VALUE)
    private Map<String, String> privateProperties;


    @Column(name = "RESULT_MESSAGE", length = Integer.MAX_VALUE)
    private String resultMessage;
}