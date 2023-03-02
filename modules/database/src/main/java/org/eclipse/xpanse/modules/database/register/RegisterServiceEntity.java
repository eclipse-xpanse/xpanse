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
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.CreateModifiedTime;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.ServiceState;
import org.hibernate.annotations.Type;

/**
 * Represents the SERVICE_STATUS table in the database.
 */
@Table(name = "REGISTER_SERVICE")
@Entity
@Data
public class RegisterServiceEntity extends CreateModifiedTime {

    @Column(name = "ID")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "OCL", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = OclConverter.class)
    private Ocl ocl;

    @Column(name = "SERVICE_STATUS")
    @Enumerated(EnumType.STRING)
    private ServiceState serviceState;

}
