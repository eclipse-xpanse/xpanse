/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;

/**
 * PoliciesEntity for persistence.
 */
@Data
@Table(name = "POLICIES", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"USERID", "POLICY", "CSP"})
})
@Entity
@EqualsAndHashCode(callSuper = true)
public class PolicyEntity extends CreateModifiedTime {

    /**
     * The id of the entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    /**
     * The id of user who created the policy.
     */
    private String userId;

    /**
     * The valid policy created by the user.
     */
    private String policy;

    /**
     * The csp which the policy belongs to.
     */
    @Enumerated(EnumType.STRING)
    private Csp csp;

    /**
     * Is the policy enabled.
     */
    @Column(columnDefinition = "boolean default true")
    private Boolean enabled;

}
