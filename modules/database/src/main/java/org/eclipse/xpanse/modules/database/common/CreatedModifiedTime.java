/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Represents the createdTime and modifiedTime of the entry in the database. */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class CreatedModifiedTime {

    @CreatedDate
    @Column(name = "CREATED_TIME")
    private OffsetDateTime createdTime;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_TIME")
    private OffsetDateTime lastModifiedTime;
}
