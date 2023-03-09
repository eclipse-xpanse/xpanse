/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.register;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.hibernate.annotations.Type;

/**
 * Represents the REGISTER_SERVICE table in the database.
 */
@Table(name = "REGISTER_SERVICE", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"NAME", "VERSION", "CSP", "CATEGORY"})
})
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterServiceEntity extends CreateModifiedTime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "VERSION", nullable = false)
    private String version;

    @Column(name = "CSP", nullable = false)
    @Enumerated(EnumType.STRING)
    private Csp csp;

    @Column(name = "CATEGORY")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "OCL", columnDefinition = "json", nullable = false)
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Ocl ocl;

    @Column(name = "SERVICE_STATE")
    @Enumerated(EnumType.STRING)
    private ServiceState serviceState;
}
