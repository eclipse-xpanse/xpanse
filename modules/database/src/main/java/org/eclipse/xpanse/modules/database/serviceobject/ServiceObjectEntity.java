/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceobject;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.hibernate.annotations.Type;

/** ServiceObjectEntity for persistence. */
@Table(name = "SERVICE_OBJECT")
@Entity
@Data
public class ServiceObjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OBJECT_ID", nullable = false)
    private UUID objectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID", nullable = false)
    private ServiceDeploymentEntity serviceDeploymentEntity;

    @Column(name = "OBJECT_TYPE", nullable = false)
    private String objectType;

    @Column(name = "OBJECT_IDENTIFIER_NAME", nullable = false)
    private String objectIdentifierName;

    @Column(name = "PROPERTIES", columnDefinition = "json", length = Integer.MAX_VALUE)
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> properties;

    @ElementCollection
    @CollectionTable(
            name = "SERVICE_DEPENDENT_OBJECT",
            joinColumns = @JoinColumn(name = "OBJECT_ID", nullable = false))
    @Column(name = "DEPENDENT_OBJECT_ID")
    private Set<UUID> dependentObjectIds;

    @ElementCollection
    @CollectionTable(
            name = "SERVICE_OBJECT_ORDER_HISTORY",
            joinColumns = @JoinColumn(name = "OBJECT_ID", nullable = false),
            foreignKey =
                    @ForeignKey(
                            name = "FK_ORDER_HISTORY",
                            foreignKeyDefinition =
                                    "FOREIGN KEY (ORDER_ID) REFERENCES SERVICE_ORDER(ORDER_ID)"))
    @Column(name = "ORDER_ID")
    private Set<UUID> objectOrderIds;
}
