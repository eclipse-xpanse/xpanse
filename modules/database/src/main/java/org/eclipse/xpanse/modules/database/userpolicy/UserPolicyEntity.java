/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.userpolicy;

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
import org.eclipse.xpanse.modules.database.common.CreatedModifiedTime;
import org.eclipse.xpanse.modules.models.common.enums.Csp;

/** Represents the USER_POLICY table in the database. */
@Data
@Table(
        name = "USER_POLICY",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"USERID", "POLICY", "CSP"})})
@Entity
@EqualsAndHashCode(callSuper = true)
public class UserPolicyEntity extends CreatedModifiedTime {

    /** The id of the entity. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    /** The id of user who created the policy. */
    @Column(name = "USER_ID", nullable = false)
    private String userId;

    /** The valid policy created by the user. */
    @Column(name = "POLICY", length = Integer.MAX_VALUE, nullable = false)
    private String policy;

    /** The csp which the policy belongs to. */
    @Enumerated(EnumType.STRING)
    @Column(name = "CSP", nullable = false)
    private Csp csp;

    /** Is the policy enabled. */
    @Column(columnDefinition = "boolean default true", nullable = false)
    private Boolean enabled;
}
