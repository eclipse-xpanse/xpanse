/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicepolicy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** Represents the SERVICE_POLICY table in the database. */
@Data
@Table(
        name = "SERVICE_POLICY",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"SERVICE_TEMPLATE_ID", "POLICY"})})
@Entity
@EqualsAndHashCode(callSuper = true)
public class ServicePolicyEntity extends CreateModifiedTime {

    /** The id of the entity. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    /** The valid policy created by the user. */
    @Column(name = "POLICY", length = Integer.MAX_VALUE)
    private String policy;

    /** The registered service template we belonged to. */
    @ManyToOne
    @JoinColumn(name = "SERVICE_TEMPLATE_ID", nullable = false)
    @JsonIgnoreProperties({"servicePolicyEntity"})
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ServiceTemplateEntity serviceTemplate;

    @Column(name = "FLAVOR_NAMES")
    private String flavorNames;

    /** Is the policy enabled. */
    @Column(columnDefinition = "boolean default true")
    private Boolean enabled;
}
